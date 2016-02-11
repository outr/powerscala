package org.powerscala.ref

/**
 * HardReference represents a reference that will never be cleared except with an explicit call to clear().
 */
class HardReference[T <: AnyRef] private(private var value: T) extends Reference[T] {
  def apply() = value

  def clear() = value = null.asInstanceOf[T]

  def enqueue() = if (value != null) {
    clear()
    true
  } else {
    false
  }

  def get = Option(value)

  def getOrNull = value

  def isEnqueued = value == null
}

object HardReference {
  def apply[T <: AnyRef](value: T) = new HardReference(value)
}