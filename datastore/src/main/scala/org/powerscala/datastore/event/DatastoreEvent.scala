package org.powerscala.datastore.event

import org.powerscala.event.Event
import org.powerscala.datastore.{DatastoreCollection, Identifiable}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreEvent[T <: Identifiable] extends Event {
  def obj: T

  def collection: DatastoreCollection[T]
}

case class DatastorePersist[T <: Identifiable](collection: DatastoreCollection[T], obj: T) extends DatastoreEvent[T]

case class DatastoreDelete[T <: Identifiable](collection: DatastoreCollection[T], obj: T) extends DatastoreEvent[T]