package org.powerscala.datastore

import org.powerscala.event.Listenable
import org.powerscala.hierarchy.ChildLike
import org.powerscala.datastore.event.{DatastoreDeleteProcessor, DatastorePersistProcessor}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreSession extends Listenable with ChildLike[Datastore] {
  def datastore: Datastore

  protected def hierarchicalParent = datastore

  val persists = new DatastorePersistProcessor
  val deletes = new DatastoreDeleteProcessor

  def apply[T <: Identifiable](implicit manifest: Manifest[T]) = collection[T](null)(manifest)

  def delete(): Unit

  final def collection[T <: Identifiable](name: String = null)(implicit manifest: Manifest[T]) = {
    val n = datastore.aliasName(name, manifest.runtimeClass)
    val creator = (name: String) => createCollection[T](name)(manifest)
    datastore.creatingCollection[T](n, this)(manifest)
    val collection = datastore.createCollection[T](n, this, creator)(manifest)
    datastore.createdCollection[T](n, this, collection)(manifest)
    collection
  }

  protected def createCollection[T <: Identifiable](name: String)(implicit manifest: Manifest[T]): DatastoreCollection[T]

  def disconnect(): Unit
}