package org.powerscala.datastore

import java.util
import query.Field

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Lazy[T <: Identifiable] extends Function0[T] with Identifiable {
  def manifest: Manifest[T]

  def loaded: Boolean

  def get() = apply() match {
    case null => None
    case r => Some(r)
  }

  def getOrElse(f: => T) = apply() match {
    case null => f
    case r => r
  }

  override def toString() = "Lazy(%s)".format(apply())
}

object Lazy {
  def id[T <: Identifiable] = Field.id[Lazy[T]]

  def apply[T <: Identifiable](value: T)(implicit manifest: Manifest[T]) = StaticLazy(value)

  def apply[T <: Identifiable](id: util.UUID, datastore: Datastore, collectionName: String)(implicit manifest: Manifest[T]) = {
    LazyValue(id, datastore, collectionName)
  }
}

case class StaticLazy[T <: Identifiable](value: T)(implicit val manifest: Manifest[T]) extends Lazy[T] {
  def id = value.id
  def loaded = true

  def apply() = value
}

case class LazyValue[T <: Identifiable](id: util.UUID, datastore: Datastore, collectionName: String)(implicit val manifest: Manifest[T]) extends Lazy[T] {
//  private lazy val value = collection.byId(id).getOrElse(null.asInstanceOf[T])    // TODO: remove the asInstanceOf[T]
  private var _loaded = false
  def loaded = _loaded

  private lazy val value = datastore {
    case session => {
      val v = session.collection[T](collectionName)(manifest).byId(id).getOrElse(null.asInstanceOf[T])
      _loaded = true
      v
    }
  }

  def apply() = value
}