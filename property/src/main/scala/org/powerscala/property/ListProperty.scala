package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ListProperty[T] extends Property[List[T]] {
  def +=(t: T) = synchronized {
    this := (t :: value.reverse).reverse
  }
  def -=(t: T) = synchronized {
    this := value.filterNot(p => p == t)
  }
  def isEmpty = value.isEmpty
  def nonEmpty = value.nonEmpty
  def ++=(seq: Seq[T]) = synchronized {
    this := (seq.toList.reverse ::: value.reverse).reverse
  }
  def contains(t: T) = value.contains(t)
}