package org.powerscala.property.backing

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Backing[T] {
  protected[property] def getValue: T

  protected[property] def setValue(value: T): Unit
}
