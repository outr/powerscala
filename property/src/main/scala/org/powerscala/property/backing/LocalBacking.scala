package org.powerscala.property.backing

/**
 * LocalBacking utilizes a ThreadLocal for the backing store.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class LocalBacking[T] extends Backing[T] {
  private val v = new ThreadLocal[T]()

  final def getValue = v.get()

  final def setValue(value: T) {
    v.set(value)
  }
}