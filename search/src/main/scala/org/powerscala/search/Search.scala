package org.powerscala.search

import java.io.File
import org.apache.lucene.store.{RAMDirectory, FSDirectory}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.Version
import org.apache.lucene.index.{Term, DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.search.{TopDocs, MultiCollector, TopScoreDocCollector, IndexSearcher}
import org.apache.lucene.document.{StringField, Document, Field}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.facet.taxonomy.{TaxonomyReader, CategoryPath}
import org.apache.lucene.facet.taxonomy.directory.{DirectoryTaxonomyReader, DirectoryTaxonomyWriter}
import org.apache.lucene.facet.index.FacetFields
import scala.collection.JavaConversions._
import org.apache.lucene.facet.params.FacetSearchParams
import org.apache.lucene.facet.search.{FacetRequest, FacetResult, FacetsCollector, CountFacetRequest}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Search(defaultField: String, val directory: Option[File] = None, append: Boolean = true, ramBufferInMegs: Double = 256.0) {
  private val version = Version.LUCENE_46
  private val indexDir = directory match {
    case Some(d) => FSDirectory.open(new File(d, "index"))
    case None => new RAMDirectory
  }
  val analyzer = new StandardAnalyzer(version)

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
  protected val facetFields = new FacetFields(taxonomyWriter)

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

  def update(du: DocumentUpdate) = {
    val id = du.fields.head
    if (!id.isInstanceOf[StringField]) throw new RuntimeException(s"Attempting to update with non-StringField term (${id.getClass}). Not supported.")
    val term = new Term(id.name(), id.stringValue())
    val doc = new Document
    du.fields.foreach {
      case f => doc.add(f)
    }

    // Set facet fields
    if (du.paths.nonEmpty) {
      facetFields.addFields(doc, du.paths)
    }

    writer.updateDocument(term, doc)
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

  def commit() = writer.commit()

  val query = SearchQueryBuilder(this, defaultField)

  def query(q: String): SearchQueryBuilder = query.copy(queryString = q)

  private[search] def search(q: SearchQueryBuilder) = {
    val parser = new QueryParser(version, defaultField, analyzer)
    parser.setAllowLeadingWildcard(q.allowLeadingWildcard)
    val query = parser.parse(q.queryString)
    val collector = TopScoreDocCollector.create(q.offset + q.limit, null, false)

    var fc: FacetsCollector = null
    val collectors = if (q.facetRequests.nonEmpty) {
      val fsp = new FacetSearchParams(q.facetRequests: _*)
      fc = FacetsCollector.create(fsp, searcher.getIndexReader, taxonomyReader)
      MultiCollector.wrap(fc, collector)
    } else {
      collector
    }

    searcher.search(query, collectors)

    val topDocs = collector.topDocs(q.offset, q.limit)

    val facetResults = if (q.facetRequests.nonEmpty) {
      fc.getFacetResults.toList
    } else {
      Nil
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

case class DocumentUpdate(fields: List[Field], paths: List[CategoryPath])

object DocumentUpdate {
  def apply(fields: Field*): DocumentUpdate = DocumentUpdate(fields.toList, Nil)
}

case class SearchQueryBuilder(instance: Search,
                              defaultField: String,
                              queryString: String = "*",
                              offset: Int = 0,
                              limit: Int = 100,
                              allowLeadingWildcard: Boolean = true,
                              facetRequests: List[FacetRequest] = Nil) {
  def facets(facetRequests: FacetRequest*) = copy(facetRequests = facetRequests.toList)
  def facet(name: String, max: Int = 10) = copy(facetRequests = new CountFacetRequest(new CategoryPath(name), max) :: facetRequests)
  def run() = instance.search(this)

  def offset(o: Int): SearchQueryBuilder = copy(offset = o)
  def limit(l: Int): SearchQueryBuilder = copy(limit = l)
}

case class SearchResults(topDocs: TopDocs, facetResults: List[FacetResult], b: SearchQueryBuilder) {
  def scoreDocs = topDocs.scoreDocs
  def pageStart = b.offset
  def pageSize = b.limit
  def pages = math.ceil(total.toDouble / pageSize.toDouble).toInt
  def page = pageStart / pageSize
  def total = topDocs.totalHits
  def facets(name: String) = facetResults.collectFirst {
    case r if r.getFacetResultNode.label.components(0) == name => r.getFacetResultNode.subResults.map(n => Facet(n.label.components(1), n.value)).toList
  }
  def page(p: Int) = {
    val pageNumber = math.min(pages - 1, math.max(0, p))
    // Offset to the proper page, remove all facets (no need to query them again), and then set the facetResults
    b.offset(pageNumber * b.limit).facets().run().copy(facetResults = facetResults)
  }
  def previousPage = page(page - 1)
  def nextPage = page(page + 1)
}

case class Facet(name: String, value: Double)