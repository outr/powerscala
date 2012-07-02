package org.powerscala.property.event

import org.powerscala.event.ChangeEvent
import org.powerscala.property.Property

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class PropertyChangeEvent(property: Property[_], oldValue: Any, newValue: Any) extends ChangeEvent