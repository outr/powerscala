package org.powerscala.datastore

import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Lazy[T <: Identifiable] extends Function0[T] with Identifiable {
  def manifest: Manifest[T]

  override def toString() = "Lazy(%s)".format(apply())
}

object Lazy {
  def apply[T <: Identifiable](value: T)(implicit manifest: Manifest[T]) = StaticLazy(value)

  def apply[T <: Identifiable](id: util.UUID, datastore: Datastore, collectionName: String)(implicit manifest: Manifest[T]) = {
    LazyValue(id, datastore, collectionName)
  }
}

case class StaticLazy[T <: Identifiable](value: T)(implicit val manifest: Manifest[T]) extends Lazy[T] {
  def id = value.id

  def apply() = value
}

case class LazyValue[T <: Identifiable](id: util.UUID, datastore: Datastore, collectionName: String)(implicit val manifest: Manifest[T]) extends Lazy[T] {
//  private lazy val value = collection.byId(id).getOrElse(null.asInstanceOf[T])    // TODO: remove the asInstanceOf[T]
  private lazy val value = datastore {
    case session => {
      session.collection[T](collectionName)(manifest).byId(id).getOrElse(null.asInstanceOf[T])
    }
  }

  def apply() = value
}