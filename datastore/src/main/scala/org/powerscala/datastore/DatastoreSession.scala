package org.powerscala.datastore

import org.powerscala.event.Listenable
import org.powerscala.hierarchy.Child


/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreSession extends Listenable with Child {
  def datastore: Datastore
  def parent = datastore

  override def bus = datastore.bus

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