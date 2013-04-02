package org.powerscala.property.event

import org.powerscala.property.Property
import org.powerscala.event.Event

import language.existentials

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class PropertyChangingEvent(property: Property[_], oldValue: Any, newValue: Any) extends Event
