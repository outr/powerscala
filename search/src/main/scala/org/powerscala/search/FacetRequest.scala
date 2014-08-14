package org.powerscala.search

import org.apache.lucene.facet.LabelAndValue

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FacetRequest(drillDown: DrillDown, limit: Int, filter: Option[LabelAndValue => Boolean], excludeDrillDown: Boolean = true)
