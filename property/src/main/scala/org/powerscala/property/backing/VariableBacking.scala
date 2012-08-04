package org.powerscala.property.backing

/**
 * VariableBacking utilizes a standard var for the backing store.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class VariableBacking[T] extends Backing[T] {
  private var v: T = _

  final def getValue = v

  final def setValue(value: T) {
    this.v = value
  }
}