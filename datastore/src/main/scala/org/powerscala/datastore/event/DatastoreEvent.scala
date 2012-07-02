package org.powerscala.datastore.event

import org.powerscala.event.Event
import org.powerscala.datastore.{DatastoreCollection, Persistable}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreEvent[T <: Persistable] extends Event {
  def obj: T

  def collection: DatastoreCollection[T]
}

case class DatastorePersist[T <: Persistable](collection: DatastoreCollection[T], obj: T) extends DatastoreEvent[T]

case class DatastoreDelete[T <: Persistable](collection: DatastoreCollection[T], obj: T) extends DatastoreEvent[T]