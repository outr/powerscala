package org.powerscala.property

import scala.collection.immutable.ListSet

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ListSetProperty[T] extends Property[ListSet[T]] {
  def +=(t: T) = this := value + t
  def -=(t: T) = this := value - t
  def isEmpty = value.isEmpty
  def nonEmpty = value.nonEmpty
  def ++=(seq: Seq[T]) = this := value ++ seq
  def contains(t: T) = value.contains(t)

  def :=(seq: Seq[T]) = apply(ListSet(seq: _*))
}