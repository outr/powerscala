package org.powerscala.property.backing

/**
 * VolatileVariableBacking utilizes a volatile var for the backing store.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class VolatileVariableBacking[T] extends Backing[T] {
  @volatile
  private var v: T = _

  protected[property] final def getValue = v

  protected[property] final def setValue(value: T) {
    this.v = value
  }
}