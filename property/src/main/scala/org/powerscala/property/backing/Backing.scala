package org.powerscala.property.backing

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Backing[T] {
  def getValue: T

  def setValue(value: T): Unit
}
