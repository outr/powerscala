package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait SetProperty[T] extends Property[Set[T]] {
  def +=(t: T) = this := value + t
  def -=(t: T) = this := value - t
  def isEmpty = value.isEmpty
  def nonEmpty = value.nonEmpty
  def ++=(seq: Seq[T]) = this := value ++ seq
  def contains(t: T) = value.contains(t)
}