package org.powerscala.property.event

import org.powerscala.property.Property
import org.powerscala.event.Change

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class PropertyChangeEvent[T](property: Property[T], oldValue: T, newValue: T) extends Change[T]