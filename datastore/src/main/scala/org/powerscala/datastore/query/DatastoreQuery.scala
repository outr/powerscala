package org.powerscala.datastore.query

import org.powerscala.datastore._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class DatastoreQuery[T <: Identifiable](collection: DatastoreCollection[T],
                                            _skip: Int = 0,
                                            _limit: Int = Int.MaxValue,
                                            _filters: List[Filter[T]] = List.empty[Filter[T]],
                                            _sort: List[Sort[T, _]] = List.empty[Sort[T, _]]) extends Iterable[T] {
  def skip(s: Int) = copy(_skip = s)

  def limit(l: Int) = copy(_limit = l)

  def filter(filter: Filter[T]) = copy(_filters = filter :: _filters)

  def sort(sort: Sort[T, _]) = copy(_sort = sort :: _sort)

  def iterator = collection.executeQuery(this)

  def ids = collection.executeQueryIds(this)

  override def toString() = "DatastoreQuery(%s, skip = %s, limit = %s, filters = %s)".format(collection.name, _skip, _limit, _filters)
}