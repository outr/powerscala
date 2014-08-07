package org.powerscala.search

import com.spatial4j.core.distance.DistanceUtils
import org.apache.lucene.facet.LabelAndValue
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.spatial.query.{SpatialOperation, SpatialArgs}
import org.apache.lucene.util.NumericUtils

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class SearchQueryBuilder(instance: Search,
                              defaultField: String,
                              queryString: Option[String] = None,
                              queries: List[(Query, BooleanClause.Occur)] = Nil,
                              offset: Int = 0,
                              limit: Int = 100,
                              allowLeadingWildcard: Boolean = true,
                              facetRequests: List[FacetRequest] = Nil,
                              drillDown: List[DrillDown] = Nil,
                              sort: Sort = Sort.RELEVANCE,
                              filter: Filter = null) {
  def queryString(query: String): SearchQueryBuilder = copy(queryString = Some(query))
  def add(query: Query, clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = copy(queries = query -> clause :: queries)
  def term(text: String, field: String = defaultField, clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(new TermQuery(new Term(field, text)), clause)
  }
  def prefix(text: String, field: String = defaultField, clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(new PrefixQuery(new Term(field, text)), clause)
  }
  def phrase(text: String, field: String = defaultField, clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    val query = new PhraseQuery
    text.split(" ").foreach(word => query.add(new Term(field, word)))
    add(query, clause)
  }
  def regexp(regex: String, field: String = defaultField, clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(new RegexpQuery(new Term(field, regex)), clause)
  }
  def wildcard(text: String, field: String = defaultField, clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(new WildcardQuery(new Term(field, text)), clause)
  }
  def fuzzy(text: String,
            field: String = defaultField,
            maxEdits: Int = FuzzyQuery.defaultMaxEdits,
            prefixLength: Int = FuzzyQuery.defaultPrefixLength,
            maxExpansions: Int = FuzzyQuery.defaultMaxExpansions,
            transpositions: Boolean = FuzzyQuery.defaultTranspositions,
            clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(new FuzzyQuery(new Term(field, text), maxEdits, prefixLength, maxExpansions, transpositions), clause)
  }
  def doubleRange(min: Double = Double.MinValue,
                  max: Double = Double.MaxValue,
                  field: String = defaultField,
                  minInclusive: Boolean = true,
                  maxInclusive: Boolean = true,
                  precisionStep: Int = NumericUtils.PRECISION_STEP_DEFAULT,
                  clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(NumericRangeQuery.newDoubleRange(field, precisionStep, min, max, minInclusive, maxInclusive), clause)
  }
  def floatRange(min: Float = Float.MinValue,
                 max: Float = Float.MaxValue,
                 field: String = defaultField,
                 minInclusive: Boolean = true,
                 maxInclusive: Boolean = true,
                 precisionStep: Int = NumericUtils.PRECISION_STEP_DEFAULT_32,
                 clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(NumericRangeQuery.newFloatRange(field, precisionStep, min, max, minInclusive, maxInclusive), clause)
  }
  def longRange(min: Long = Long.MinValue,
                max: Long = Long.MaxValue,
                field: String = defaultField,
                minInclusive: Boolean = true,
                maxInclusive: Boolean = true,
                precisionStep: Int = NumericUtils.PRECISION_STEP_DEFAULT,
                clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(NumericRangeQuery.newLongRange(field, precisionStep, min, max, minInclusive, maxInclusive), clause)
  }
  def intRange(min: Int = Int.MinValue,
               max: Int = Int.MaxValue,
               field: String = defaultField,
               minInclusive: Boolean = true,
               maxInclusive: Boolean = true,
               precisionStep: Int = NumericUtils.PRECISION_STEP_DEFAULT_32,
               clause: BooleanClause.Occur = BooleanClause.Occur.MUST) = {
    add(NumericRangeQuery.newIntRange(field, precisionStep, min, max, minInclusive, maxInclusive), clause)
  }
  def facets(facetRequests: FacetRequest*) = copy(facetRequests = facetRequests.toList)
  def facet(name: String, max: Int = 10, filter: Option[LabelAndValue => Boolean] = None) = {
    copy(facetRequests = FacetRequest(DrillDown(name), max, filter) :: facetRequests)
  }
  def drillDown(facet: String, values: String*): SearchQueryBuilder = copy(drillDown = DrillDown(facet, values: _*) :: drillDown)
  def sort(s: Sort): SearchQueryBuilder = copy(sort = s)
  def sortFromPoint(latitude: Double, longitude: Double, reverse: Boolean = false) = {
    val point = instance.point(latitude, longitude)
    val valueSource = instance.spatialStrategy.makeDistanceValueSource(point, DistanceUtils.DEG_TO_KM)
    val distanceSortField = valueSource.getSortField(reverse)
    addSort(distanceSortField)
  }
  def addSort(field: SortField) = {
    val sort = if (this.sort == null) {
      new Sort()
    } else {
      this.sort
    }
    val fields = sort.getSort :+ field
    copy(sort = new Sort(fields: _*).rewrite(instance.searcher))
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

  private[search] def createQuery() = if (queries.nonEmpty || queryString.nonEmpty) {
    val query = new BooleanQuery
    queries.foreach {
      case (q, clause) => query.add(q, clause)
    }
    queryString match {
      case Some(qs) => {
        val parser = new QueryParser(instance.version, defaultField, instance.analyzer)
        parser.setAllowLeadingWildcard(allowLeadingWildcard)
        query.add(parser.parse(qs), BooleanClause.Occur.MUST)
      }
      case None => // Ignore
    }
    query
  } else {
    new MatchAllDocsQuery
  }
}