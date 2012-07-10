package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Queryable[T <: Identifiable] {
  def or(filters: Filter[T]*) = SubFilter(Operator.or, filters)

  def and(filters: Filter[T]*) = SubFilter(Operator.and, filters)
}
