package org.powerscala.property

import event.PropertyChangeEvent
import org.powerscala.reflect._
import org.powerscala.Logging

/**
 * Binds a top-level property and a hierarchically lower property together so a change to one updates the other.
 *
 * Simple example:
 *
 * case class Person(name: String)
 * val property = Property[Person]()
 * val valueProperty = Property[String]()
 * val binding = CaseClassBinding(property, "name", valueProperty)
 * binding.updateValueProperty()
 *
 * @param property defines the top-level property that contains the value bound hierarchically by name
 * @param name the dot-separated name to access the valueProperty value within the property
 * @param valueProperty the container of the value
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class CaseClassBinding(property: StandardProperty[_],
                            name: String,
                            valueProperty: StandardProperty[Any],
                            var valueUpdatesProperty: Boolean = true,
                            var propertyUpdatesValue: Boolean = true) extends Logging {
  val propertyClazz: EnhancedClass = property.manifest.erasure
  // Manage property -> valueProperty
  val propertyListener = property.listeners.synchronous {
    case evt: PropertyChangeEvent => if (propertyUpdatesValue) {
      updateValueProperty()
    }
  }
  // Manage valueProperty -> property
  val valuePropertyListener = valueProperty.listeners.synchronous {
    case evt: PropertyChangeEvent => if (valueUpdatesProperty) {
      updateProperty()
    }
  }

  /**
   * Updates the valueProperty to reflect the value contained in property
   */
  def updateValueProperty(): Unit = {
    try {
      // property changed, apply new value to valueProperty
      val value = propertyClazz.value[Any](property().asInstanceOf[AnyRef], name)
      val currentValue = valueProperty()
      debug("propertyListener PropertyChangeEvent: %s / %s".format(value, valueProperty))
      if (currentValue != value) {
        valueProperty := value
      }
    } catch {
      case t: Throwable => error("Error on updateProperty", t)
    }
  }

  /**
   * Updates the property to reflect the value contained in valueProperty
   */
  def updateProperty(): Unit = {
    try {
      // value property changed, apply new value to property
      val value = valueProperty()
      val currentValue = propertyClazz.value[Any](property().asInstanceOf[AnyRef], name)
      debug("valuePropertyListener PropertyChangeEvent: %s / %s".format(value, currentValue))
      if (value != currentValue) {
        val updated = propertyClazz.modify[Any](property(), name, value)
        property.asInstanceOf[StandardProperty[Any]] := updated
      }
    } catch {
      case t: Throwable => error("Error on updateValueProperty", t)
    }
  }

  /**
   * Disconnects the listeners and stops this binding having any further affect
   */
  def disconnect(): Unit = {
    property.listeners -= propertyListener
    valueProperty.listeners -= valuePropertyListener
  }
}
