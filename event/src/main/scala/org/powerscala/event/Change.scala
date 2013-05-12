package org.powerscala.event

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Change[T] {
  def oldValue: T
  def newValue: T
}

object Change {
  def apply[T](oldValue: T, newValue: T) = SimpleChange(oldValue, newValue)
}

case class SimpleChange[T](oldValue: T, newValue: T) extends Change[T]