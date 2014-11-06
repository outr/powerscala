package org.powerscala.property

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ReadOnlyPropertyLense[T](property: Property[T]) extends ReadProperty[T] {
  override def apply() = property()

  def read = property.read
  def change = property.change
  def get = property.get
}
