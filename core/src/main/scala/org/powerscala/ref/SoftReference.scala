package org.powerscala.ref

import ref.{SoftReference => JSR}

/**
 * SoftReference wraps Scala's SoftReference with additional features.
 */
class SoftReference[T <: AnyRef] private(private val ref: JSR[T]) extends Reference[T] {
  def apply() = ref.apply()

  def clear() = ref.clear()

  def enqueue() = ref.enqueue()

  def get = ref.get

  def getOrNull = ref.underlying.get()

  def isEnqueued = ref.isEnqueued()
}

object SoftReference {
  def apply[T <: AnyRef](value: T) = new SoftReference[T](new JSR(value))
}