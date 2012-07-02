package org.powerscala.ref

import scala.ref.{WeakReference => JWR}

/**
 * WeakReference wraps Scala's WeakReference with additional features.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class WeakReference[T <: AnyRef] private(private val ref: JWR[T]) extends Reference[T] {
  def apply() = ref.apply()

  def clear() = ref.clear()

  def enqueue() = ref.enqueue()

  def get = ref.get

  def getOrNull = ref.underlying.get()

  def isEnqueued = ref.isEnqueued()
}

object WeakReference {
  def apply[T <: AnyRef](value: T) = new WeakReference(new JWR(value))
}