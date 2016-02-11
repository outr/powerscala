package org.powerscala.ref

/**
 * Reference wraps Scala's Reference objects to provide additional functionality.
 */
trait Reference[T <: AnyRef] extends Function0[T] {
  def apply(): T

  def clear(): Unit

  def enqueue(): Boolean

  def get: Option[T]

  def getOrNull: T

  def isEnqueued: Boolean

  def isCleared = get.isEmpty

  /**
   * Referential equality occurs on both the Reference and the wrapped object.
   */
  override def equals(obj: Any) = if (super.equals(obj)) {
    true
  } else {
    getOrNull == obj
  }
}