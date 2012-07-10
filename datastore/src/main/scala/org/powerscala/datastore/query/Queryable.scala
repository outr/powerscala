package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Queryable[T <: Identifiable] {
  def or(filters: Filter[T]*) = OrFilter(filters)

  def and(filters: Filter[T]*) = AndFilter(filters)
}
