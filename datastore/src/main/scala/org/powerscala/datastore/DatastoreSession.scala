package org.powerscala.datastore


/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreSession {
  def datastore: Datastore

  private var collections = Map.empty[String, DatastoreCollection[_]]

  def apply[T <: Persistable](implicit manifest: Manifest[T]) = collection[T](null)(manifest)

  final def collection[T <: Persistable](name: String = null)(implicit manifest: Manifest[T]) = {
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

  protected def createCollection[T <: Persistable](name: String)(implicit manifest: Manifest[T]): DatastoreCollection[T]

  def disconnect(): Unit
}