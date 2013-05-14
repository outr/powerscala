package org.powerscala.property.backing

import org.powerscala.property.Property

/**
 * @author Matt Hicks <matt@outr.com>
 */
class MapPropertyBacking[T](key: String, mapProperty: Property[Map[String, Any]]) extends Backing[Option[T]] {
  def getValue = mapProperty().get(key).asInstanceOf[Option[T]]

  def setValue(value: Option[T]) = mapProperty.synchronized {
    mapProperty := mapProperty() + (key -> value)
  }
}