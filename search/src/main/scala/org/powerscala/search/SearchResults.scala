package org.powerscala.search

import org.apache.lucene.facet.{LabelAndValue, FacetResult}
import org.apache.lucene.search.TopDocs

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class SearchResults(topDocs: TopDocs, facetResults: Map[String, List[LabelAndValue]], b: SearchQueryBuilder) {
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
