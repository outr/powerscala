package org.powerscala.property

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait ListProperty[T] extends MutableProperty[List[T]] {
  def +=(t: T) = this := (t :: value.reverse).reverse
  def -=(t: T) = this := value.filterNot(p => p == t)
  def isEmpty = value.isEmpty
  def nonEmpty = value.nonEmpty
}
