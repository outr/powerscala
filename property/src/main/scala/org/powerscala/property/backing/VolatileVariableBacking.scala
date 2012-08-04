package org.powerscala.property.backing

/**
 * VolatileVariableBacking utilizes a volatile var for the backing store.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class VolatileVariableBacking[T] extends Backing[T] {
  @volatile
  private var v: T = _

  final def getValue = v

  final def setValue(value: T) {
    this.v = value
  }
}