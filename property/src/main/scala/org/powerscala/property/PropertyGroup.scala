package org.powerscala.property

import org.powerscala.event.processor.ProcessorGroup

/**
 * PropertyGroup is a convenience wrapper around multiple properties of the same type.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyGroup[T](val properties: List[Property[T]]) extends PropertyLike[T] {
  lazy val read = ProcessorGroup(properties.map(p => p.read))
  lazy val changing = ProcessorGroup(properties.map(p => p.changing))
  lazy val change = ProcessorGroup(properties.map(p => p.change))

  /**
   * Retrieves the first value from this PropertyGroup or null if it is empty.
   */
  def apply() = properties.headOption.fold(null.asInstanceOf[T])(p => p.value)

  /**
   * Sets the supplied value to all properties in the group.
   */
  def apply(value: T) = properties.foreach(p => p.value = value)

  /**
   * Creates a new PropertyGroup adding in the supplied Property.
   */
  def and(property: Property[T]) = new PropertyGroup[T](properties = property :: properties)

  /**
   * Retrieves all values from the properties in this group as a List.
   */
  def values = properties.map(p => p.value)

  /**
   * Returns Some[T] if all properties in the group are equal. Otherwise None is returned.
   */
  def get = if (properties.nonEmpty) {
    properties.tail.foldLeft(properties.head.get)((current, property) => {
      if (current == None) {
        None
      } else {
        val o = property.get
        if (o == None) {
          None
        } else if (current.get == o.get) {
          current
        } else {
          None
        }
      }
    })
  } else {
    None
  }
}
