package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ListProperty[T] extends Property[List[T]] {
  def +=(t: T) = this := (t :: value.reverse).reverse
  def -=(t: T) = this := value.filterNot(p => p == t)
  def isEmpty = value.isEmpty
  def nonEmpty = value.nonEmpty
  def ++=(seq: Seq[T]) = this := (seq.toList.reverse ::: value.reverse).reverse
}