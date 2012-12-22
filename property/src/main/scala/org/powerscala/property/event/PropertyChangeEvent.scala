package org.powerscala.property.event

import org.powerscala.event.ChangeEvent
import org.powerscala.property.Property

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class PropertyChangeEvent(val property: Property[_], val oldValue: Any, val newValue: Any) extends ChangeEvent