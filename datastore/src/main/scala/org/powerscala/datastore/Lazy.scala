package org.powerscala.datastore

import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Lazy[T <: Identifiable] extends Function0[T] with Identifiable {
  override def toString() = "Lazy(%s)".format(apply())
}

object Lazy {
  def apply[T <: Identifiable](value: T) = StaticLazy(value)

  def apply[T <: Identifiable](id: util.UUID, collection: DatastoreCollection[T])(implicit manifest: Manifest[T]) = {
    LazyValue(id, collection)
  }
}

case class StaticLazy[T <: Identifiable](value: T) extends Lazy[T] {
  def id = value.id

  def apply() = value
}

case class LazyValue[T <: Identifiable](id: util.UUID, collection: DatastoreCollection[T])(implicit manifest: Manifest[T]) extends Lazy[T] {
  private lazy val value = collection.byId(id).getOrElse(null.asInstanceOf[T])    // TODO: remove the asInstanceOf[T]

  def apply() = value
}