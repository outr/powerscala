package org.powerscala.search

import com.spatial4j.core.distance.DistanceUtils
import org.apache.lucene.search.{Filter, Sort}
import org.apache.lucene.spatial.query.{SpatialOperation, SpatialArgs}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class SearchQueryBuilder(instance: Search,
                              defaultField: String,
                              queryString: String = "*",
                              offset: Int = 0,
                              limit: Int = 100,
                              allowLeadingWildcard: Boolean = true,
                              facetRequests: List[FacetRequest] = Nil,
                              drillDown: List[DrillDown] = Nil,
                              sort: Sort = Sort.RELEVANCE,
                              filter: Filter = null) {
  def facets(facetRequests: FacetRequest*) = copy(facetRequests = facetRequests.toList)
  def facet(name: String, max: Int = 10) = copy(facetRequests = FacetRequest(DrillDown(name), max) :: facetRequests)
  def drillDown(facet: String, values: String*): SearchQueryBuilder = copy(drillDown = DrillDown(facet, values: _*) :: drillDown)
  def sort(s: Sort): SearchQueryBuilder = copy(sort = s)
  def sortFromPoint(latitude: Double, longitude: Double) = {
    val point = instance.spatialContext.makePoint(latitude, longitude)
    val valueSource = instance.spatialStrategy.makeDistanceValueSource(point, DistanceUtils.DEG_TO_KM)
    val distanceSort = new Sort(valueSource.getSortField(false)).rewrite(instance.searcher)
    copy(sort = distanceSort)
  }
  def filter(f: Filter): SearchQueryBuilder = copy(filter = f)
  def filterByCircle(latitude: Double, longitude: Double, distanceInDegrees: Double) = {
    val args = new SpatialArgs(SpatialOperation.Intersects, instance.spatialContext.makeCircle(longitude, latitude, distanceInDegrees))
    val filter = instance.spatialStrategy.makeFilter(args)
    copy(filter = filter)
  }
  def run() = instance.search(this)

  def offset(o: Int): SearchQueryBuilder = copy(offset = o)
  def limit(l: Int): SearchQueryBuilder = copy(limit = l)
}