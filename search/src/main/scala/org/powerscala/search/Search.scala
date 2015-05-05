package org.powerscala.search

import java.io.File
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import com.spatial4j.core.context.SpatialContext
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, StringField}
import org.apache.lucene.facet._
import org.apache.lucene.facet.taxonomy.directory.{DirectoryTaxonomyReader, DirectoryTaxonomyWriter}
import org.apache.lucene.facet.taxonomy.{FastTaxonomyFacetCounts, TaxonomyReader}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search._
import org.apache.lucene.spatial.SpatialStrategy
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree
import org.apache.lucene.store.{FSDirectory, RAMDirectory}
import org.apache.lucene.uninverting.UninvertingReader
import org.apache.lucene.util.Version
import org.powerscala.concurrent.{Executor, Time}
import org.powerscala.log.Logging

import scala.collection.JavaConversions._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Search(defaultField: String, val directory: Option[File] = None, append: Boolean = true, ramBufferInMegs: Double = 256.0, commitDelay: Double = 30.0, facetResultsBounds: Int = 1000, sortedFields: List[SortedField] = Nil) extends Logging {
  Search.add(this)

  val version = Version.LATEST
  private val indexDir = directory match {
    case Some(d) => FSDirectory.open(new File(d, "index").toPath)
    case None => new RAMDirectory
  }
  val analyzer = new StandardAnalyzer()

  private val lastCommit = new AtomicLong(0L)
  private val committing = new AtomicBoolean(false)

  private val config = new IndexWriterConfig(analyzer)
  config.setOpenMode(if (append) OpenMode.CREATE_OR_APPEND else OpenMode.CREATE)
  config.setRAMBufferSizeMB(ramBufferInMegs)
  protected val writer = new IndexWriter(indexDir, config)
  writer.commit()     // Make sure the index is created

  // Facet functionality
  private val taxonomyDir = directory match {
    case Some(d) => FSDirectory.open(new File(d, "taxonomy").toPath)
    case None => new RAMDirectory
  }
  protected val taxonomyWriter = new DirectoryTaxonomyWriter(taxonomyDir)
  val facetsConfig = new FacetsConfig

  @volatile private var _taxonomyReader: DirectoryTaxonomyReader = _
  def taxonomyReader = synchronized {
    if (_taxonomyReader == null) {
      _taxonomyReader = new DirectoryTaxonomyReader(taxonomyWriter)
    } else {
      TaxonomyReader.openIfChanged(_taxonomyReader) match {
        case null => // Hasn't changed
        case r => _taxonomyReader = r
      }
    }
    _taxonomyReader
  }

  // Spatial functionality
  val spatialContext = SpatialContext.GEO
  var spatialStrategy: SpatialStrategy = _

  def point(latitude: Double, longitude: Double) = spatialContext.makePoint(longitude, latitude)

  // Reader / Search
  @volatile private var _reader = UninvertingReader.wrap(DirectoryReader.open(indexDir), Map(sortedFields.map(_.tuple): _*))
  @volatile private var _searcher = new IndexSearcher(_reader)

  def configureSpatialStrategy(maxLevels: Int = 11, fieldName: String = "geoSpatial") = {
    spatialStrategy = new RecursivePrefixTreeStrategy(new GeohashPrefixTree(spatialContext, maxLevels), fieldName)
  }

  def searcher = synchronized {
    DirectoryReader.openIfChanged(_reader) match {
      case null => // Hasn't changed
      case r => {
        _reader = r
        _searcher = new IndexSearcher(r)
      }
    }
    _searcher
  }

  def apply(docId: Int, fieldsToLoad: String*) = if (fieldsToLoad.isEmpty) {
    searcher.doc(docId)
  } else {
    searcher.doc(docId, fieldsToLoad.toSet)
  }

  def update(du: DocumentUpdate) = {
    val id = du.fields.head
    if (!id.isInstanceOf[StringField]) throw new RuntimeException(s"Attempting to update with non-StringField term (${id.getClass}). Not supported.")
    val term = new Term(id.name(), id.stringValue())
    val doc = new Document
    du.fields.foreach {
      case f => doc.add(f)
    }

    if (du.shapes.nonEmpty) {
      if (spatialStrategy == null) throw new NullPointerException("Search.spatialStrategy must not be null.")

      du.shapes.foreach {       // Add shapes to document
        case shape => spatialStrategy.createIndexableFields(shape).foreach {
          case f => doc.add(f)
        }
      }
    }

    val updatedDoc = facetsConfig.build(taxonomyWriter, doc)      // Update the document for FacetFields
    writer.updateDocument(term, updatedDoc)
    taxonomyWriter.commit()
  }

  def delete(du: DocumentUpdate): Unit = {
    val id = du.fields.head
    if (!id.isInstanceOf[StringField]) throw new RuntimeException(s"Attempting to update with non-StringField term (${id.getClass}). Not supported.")
    val term = new Term(id.name(), id.stringValue())
    delete(term)
  }

  def delete(term: Term): Unit = {
    writer.deleteDocuments(term)
  }

  def commit() = {
    writer.commit()
    lastCommit.set(System.currentTimeMillis())
  }

  def requestCommit() = if (committing.compareAndSet(false, true)) {    // Invoke commit if not already scheduled
    val elapsed = Time.fromMillis(System.currentTimeMillis() - lastCommit.get())
    val timeLeft = math.max(commitDelay - elapsed, 0.0)
    Executor.schedule(timeLeft) {
      commit()
      committing.set(false)
    }
  }

  def rollback() = {
    writer.rollback()
  }

  val query = SearchQueryBuilder(this, defaultField)

  def query(q: String): SearchQueryBuilder = query.copy(queryString = Some(q))

  private[search] def search(q: SearchQueryBuilder) = {
    val baseQuery = q.createQuery()
    val sort = q.sort
    val numHits = q.offset + q.limit
    val fillFields = true
    val trackDocScores = true
    val trackMaxScore = true
    if (numHits > 0) {
      val collector = TopFieldCollector.create(sort, numHits, fillFields, trackDocScores, trackMaxScore)

      val facetsCollector = if (q.facetRequests.nonEmpty) {
        new FacetsCollector
      } else {
        null
      }

      val query = if (q.drillDown.nonEmpty) {
        // Drill-down via facets query
        if (facetsCollector == null) throw new NullPointerException("Facets must be provided in order to drill-down.")
        val ddq = new DrillDownQuery(facetsConfig, baseQuery)
        q.drillDown.groupBy(dd => dd.dim).foreach {
          case (dim, list) => {
            val disableCoord = true
            val bq = new BooleanQuery(disableCoord)
            list.foreach {
              case dd => {
                val indexedField = facetsConfig.getDimConfig(dd.dim).indexFieldName
                bq.add(new TermQuery(DrillDownQuery.term(indexedField, dd.dim, dd.path: _*)), BooleanClause.Occur.MUST)
              }
            }
            ddq.add(dim, bq)
          }
        }
        ddq
      } else {
        // No drill-down, use the base query
        baseQuery
      }

      if (q.facetRequests.nonEmpty) {
        FacetsCollector.search(searcher, query, q.offset + q.limit, facetsCollector) // what should 'n' be?
      }
      searcher.search(query, q.filter, collector)

      val topDocs = collector.topDocs(q.offset, q.limit)

      val facetResults: Map[String, List[LabelAndValue]] = if (q.facetRequests.nonEmpty) {
        val facets = new FastTaxonomyFacetCounts(taxonomyReader, facetsConfig, facetsCollector)
        q.facetRequests.map {
          case fr => {
            val dim = fr.drillDown.dim
            val limit = facetResultsBounds
            val result = facets.getTopChildren(limit, fr.drillDown.dim, fr.drillDown.path: _*)
            val filter = fr.filter match {
              case Some(f) => f
              case None => Search.EmptyLabelAndValueFilter
            }
            val drillDownFilter = if (fr.excludeDrillDown) {
              (lv: LabelAndValue) => !q.hasDrillDown(dim, Seq(lv.label))
            } else {
              Search.EmptyLabelAndValueFilter
            }
            val labelValues = if (result != null) result.labelValues else Array.empty[LabelAndValue]
            val filtered = labelValues.toStream.filter(filter).filter(drillDownFilter).take(fr.limit).toList

            dim -> filtered
          }
        }.toMap
      } else {
        Map.empty
      }

      SearchResults(topDocs, facetResults, q)
    } else {
      SearchResults(null, null, q)
    }
  }

  def close() = {
    info("Closing Search instance...")
    taxonomyReader.close()
    taxonomyWriter.close()
    writer.close()
    _reader.close()
    indexDir.close()

    Search.remove(this)
    info("Search instance closed.")
  }
}

case class SortedField(name: String, sortType: UninvertingReader.Type = UninvertingReader.Type.SORTED) {
  def tuple = name -> sortType
}

object Search {
  val EmptyLabelAndValueFilter = (lv: LabelAndValue) => true

  private var items = Set.empty[Search]

  private def add(search: Search) = synchronized {
    items += search
  }

  private def remove(search: Search) = synchronized {
    items -= search
  }

  def closeAll() = items.foreach(_.close())
}