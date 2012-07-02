package org.powerscala.property

/**
 * ImmutableProperty is created once and read-only.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class ImmutableProperty[T](v: T, name: String = null) extends Property[T] {
  def apply() = v
}