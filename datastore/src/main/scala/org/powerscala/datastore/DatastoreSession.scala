package org.powerscala.datastore

import org.powerscala.event.Listenable
import org.powerscala.hierarchy.Child


/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreSession extends Listenable with Child {
  def datastore: Datastore
  def parent = datastore

  private var collections = Map.empty[String, DatastoreCollection[_]]

  def apply[T <: Identifiable](implicit manifest: Manifest[T]) = collection[T](null)(manifest)

  final def collection[T <: Identifiable](name: String = null)(implicit manifest: Manifest[T]) = {
    val n = name match {
      case null => manifest.erasure.getSimpleName
      case s => s
    }
    collections.get(n) match {
      case Some(c) => c.asInstanceOf[DatastoreCollection[T]]
      case None => {
        val c = createCollection[T](n)
        collections += n -> c
        c
      }
    }
  }

  protected def createCollection[T <: Identifiable](name: String)(implicit manifest: Manifest[T]): DatastoreCollection[T]

  def disconnect(): Unit
}