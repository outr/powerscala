package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ReadProperty[T] extends (() => T) {
  def value = apply()
}
