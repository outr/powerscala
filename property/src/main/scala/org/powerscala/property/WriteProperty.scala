package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait WriteProperty[T] extends ((T) => Unit) {
  def :=(value: T) = apply(value)
  def value_=(value: T) = apply(value)
}
