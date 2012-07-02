package org.powerscala.property.backing

import java.util.concurrent.atomic.AtomicReference

/**
 * AtomicBacking utilizes an AtomicReference for the backing store.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class AtomicBacking[T] extends Backing[T] {
  private val v = new AtomicReference[T]()

  protected[property] final def getValue = v.get()

  protected[property] final def setValue(value: T) {
    v.set(value)
  }
}