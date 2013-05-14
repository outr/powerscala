package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait PropertyLike[T] extends ((T) => Unit) with (() => T) {
  def :=(value: T) = apply(value)

  def value = apply()
  def value_=(value: T) = apply(value)
}
