package org.powerscala.property.event

import org.powerscala.event.Event
import org.powerscala.property.Property

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class PropertyValueEvent(property: Property[_], event: Event) extends Event