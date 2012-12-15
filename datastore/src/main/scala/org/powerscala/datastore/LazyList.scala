package org.powerscala.datastore

import query.Field
import java.util.UUID

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait LazyList[T <: Identifiable] extends Function0[List[T]] {
  def manifest: Manifest[T]

  def ids: List[UUID]

  def loaded: Boolean

  override def toString() = "LazyList(%s)".format(apply().mkString(", "))
}

object LazyList {
  def id[T <: Identifiable] = Field.basic[LazyList[T], UUID]("listIds")

  def empty[T <: Identifiable](implicit manifest: Manifest[T]) = StaticLazyList(Nil)

  def apply[T <: Identifiable](values: T*)(implicit manifest: Manifest[T]) = StaticLazyList(values.toList)

  def apply[T <: Identifiable](values: List[T])(implicit manifest: Manifest[T]) = StaticLazyList(values)

  def apply[T <: Identifiable](ids: List[UUID])(implicit manifest: Manifest[T]) = UUIDLazyList[T](ids)(manifest)

  def apply[T <: Identifiable](ids: List[UUID], datastore: Datastore, collectionName: String)(implicit manifest: Manifest[T]) = {
    LazyListValue(ids, datastore, collectionName)
  }
}

case class StaticLazyList[T <: Identifiable](values: List[T])(implicit val manifest: Manifest[T]) extends LazyList[T] {
  lazy val ids = values.map(v => v.id)
  def loaded = true

  def apply() = values
}

case class UUIDLazyList[T <: Identifiable](ids: List[UUID])(implicit val manifest: Manifest[T]) extends LazyList[T] {
  def loaded = false
  def apply() = null
}

case class LazyListValue[T <: Identifiable](ids: List[UUID], datastore: Datastore, collectionName: String)(implicit val manifest: Manifest[T]) extends LazyList[T] {
  private var _loaded = false
  def loaded = _loaded

  private lazy val values = datastore {
    case session => {
      val vs = session.collection[T](collectionName)(manifest).byIds(ids: _*)
      _loaded = true
      vs
    }
  }

  def apply() = values
}