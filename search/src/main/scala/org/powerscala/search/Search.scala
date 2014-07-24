package org.powerscala.search

import java.io.File
import org.apache.lucene.facet.{FacetResult, DrillDownQuery, FacetsConfig, FacetsCollector}
import org.apache.lucene.store.{RAMDirectory, FSDirectory}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.index.{Term, DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.search._
import org.apache.lucene.document.{StringField, Document, Field}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.facet.taxonomy.{FastTaxonomyFacetCounts, TaxonomyReader, CategoryPath}
import org.apache.lucene.facet.taxonomy.directory.{DirectoryTaxonomyReader, DirectoryTaxonomyWriter}
import scala.collection.JavaConversions._
import java.util.concurrent.atomic.{AtomicLong, AtomicBoolean}
import org.powerscala.concurrent.Time._
import org.powerscala.concurrent.{Executor, Time}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Search(defaultField: String, val directory: Option[File] = None, append: Boolean = true, ramBufferInMegs: Double = 256.0, commitDelay: Double = 30.seconds) {
  private val version = Version.LUCENE_4_9
  private val indexDir = directory match {
    case Some(d) => FSDirectory.open(new File(d, "index"))
    case None => new RAMDirectory
  }
  val analyzer = new StandardAnalyzer(version)

  private val lastCommit = new AtomicLong(0L)
  private val committing = new AtomicBoolean(false)

  private val config = new IndexWriterConfig(version, analyzer)
  config.setOpenMode(if (append) OpenMode.CREATE_OR_APPEND else OpenMode.CREATE)
  config.setRAMBufferSizeMB(ramBufferInMegs)
  protected val writer = new IndexWriter(indexDir, config)
  writer.commit()     // Make sure the index is created

  // Facet functionality
  private val taxonomyDir = directory match {
    case Some(d) => FSDirectory.open(new File(d, "taxonomy"))
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

  // Reader / Search
  @volatile private var _reader = DirectoryReader.open(indexDir)
  @volatile private var _searcher = new IndexSearcher(_reader)

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

  val query = SearchQueryBuilder(this, defaultField)

  def query(q: String): SearchQueryBuilder = query.copy(queryString = q)

  private[search] def search(q: SearchQueryBuilder) = {
    val parser = new QueryParser(version, defaultField, analyzer)
    parser.setAllowLeadingWildcard(q.allowLeadingWildcard)
    val baseQuery = parser.parse(q.queryString)
    val sort = q.sort
    val numHits = q.offset + q.limit
    val fillFields = true
    val trackDocScores = true
    val trackMaxScore = true
    val docsScoredInOrder = false
    val collector = TopFieldCollector.create(sort, numHits, fillFields, trackDocScores, trackMaxScore, docsScoredInOrder)

    val facetsCollector = if (q.facetRequests.nonEmpty) {
      new FacetsCollector
    } else {
      null
    }

    val query = if (q.drillDown.nonEmpty) {          // Drill-down via facets query
      if (facetsCollector == null) throw new NullPointerException("Facets must be provided in order to drill-down.")
      val ddq = new DrillDownQuery(facetsConfig, baseQuery)
      q.drillDown.foreach {
        case dd => ddq.add(dd.dim, dd.path: _*)
      }
      ddq
    } else {                                                        // No drill-down, use the base query
      baseQuery
    }

    if (q.facetRequests.nonEmpty) {
      FacetsCollector.search(searcher, query, q.offset + q.limit, facetsCollector)
    }
    searcher.search(query, collector)

    val topDocs = collector.topDocs(q.offset, q.limit)

    val facetResults: Map[String, FacetResult] = if (q.facetRequests.nonEmpty) {
      val facets = new FastTaxonomyFacetCounts(taxonomyReader, facetsConfig, facetsCollector)

      q.facetRequests.map(fr => fr.drillDown.dim -> facets.getTopChildren(fr.limit, fr.drillDown.dim, fr.drillDown.path: _*)).toMap
    } else {
      Map.empty
    }

    SearchResults(topDocs, facetResults, q)
  }

  def close() = {
    writer.close(true)
    _reader.close()
    taxonomyWriter.close()
    taxonomyReader.close()
  }
}

case class DocumentUpdate(fields: List[Field])

object DocumentUpdate {
  def apply(fields: Field*): DocumentUpdate = DocumentUpdate(fields.toList)
}

case class SearchQueryBuilder(instance: Search,
                              defaultField: String,
                              queryString: String = "*",
                              offset: Int = 0,
                              limit: Int = 100,
                              allowLeadingWildcard: Boolean = true,
                              facetRequests: List[FacetRequest] = Nil,
                              drillDown: List[DrillDown] = Nil,
                              sort: Sort = Sort.RELEVANCE) {
  def facets(facetRequests: FacetRequest*) = copy(facetRequests = facetRequests.toList)
  def facet(name: String, max: Int = 10) = copy(facetRequests = FacetRequest(DrillDown(name), max) :: facetRequests)
  def drillDown(facet: String, values: String*): SearchQueryBuilder = copy(drillDown = DrillDown(facet, values: _*) :: drillDown)
  def sort(s: Sort) = copy(sort = s)
  def run() = instance.search(this)

  def offset(o: Int): SearchQueryBuilder = copy(offset = o)
  def limit(l: Int): SearchQueryBuilder = copy(limit = l)
}

case class FacetRequest(drillDown: DrillDown, limit: Int)

case class DrillDown(dim: String, path: String*)

case class SearchResults(topDocs: TopDocs, facetResults: Map[String, FacetResult], b: SearchQueryBuilder) {
  def doc(index: Int, fieldsToLoad: String*) = b.instance.apply(topDocs.scoreDocs(index).doc, fieldsToLoad: _*)
  def docs = topDocs.scoreDocs.indices.map(index => doc(index)).toList
  def scoreDocs = topDocs.scoreDocs
  def pageStart = b.offset
  def pageSize = b.limit
  def pages = math.ceil(total.toDouble / pageSize.toDouble).toInt
  def page = pageStart / pageSize
  def total = topDocs.totalHits
  def pageTotal = scoreDocs.length
  def facets(name: String) = facetResults(name)
  def page(p: Int) = {
    val pageNumber = math.min(pages - 1, math.max(0, p))
    // Offset to the proper page, remove all facets (no need to query them again), and then set the facetResults
    b.offset(pageNumber * b.limit).facets().run().copy(facetResults = facetResults)
  }
  def previousPage = page(page - 1)
  def nextPage = page(page + 1)
  def hasPreviousPage = page > 0
  def hasNextPage = page < pages - 1
}

case class Facet(name: String, value: Double)