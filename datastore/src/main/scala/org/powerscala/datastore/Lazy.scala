package org.powerscala.datastore

import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Lazy[T <: Persistable] extends Function0[T] with Persistable {
  override def toString() = "Lazy(%s)".format(apply())
}

object Lazy {
  def apply[T <: Persistable](value: T) = StaticLazy(value)

  def apply[T <: Persistable](id: util.UUID, collection: DatastoreCollection[T])(implicit manifest: Manifest[T]) = {
    LazyValue(id, collection)
  }
}

case class StaticLazy[T <: Persistable](value: T) extends Lazy[T] {
  def id = value.id

  def apply() = value
}

case class LazyValue[T <: Persistable](id: util.UUID, collection: DatastoreCollection[T])(implicit manifest: Manifest[T]) extends Lazy[T] {
  private lazy val value = collection.byId(id).getOrElse(null.asInstanceOf[T])    // TODO: remove the asInstanceOf[T]

  def apply() = value
}