package org.powerscala.property

import event.PropertyChangeEvent
import org.powerscala.reflect._

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
case class CaseClassBinding[T <: AnyRef](property: StandardProperty[T],
                                         name: String,
                                         valueProperty: StandardProperty[Any]) {
  val propertyClazz: EnhancedClass = property.manifest.erasure
  // Manage property -> valueProperty
  val propertyListener = property.listeners.synchronous {
    case evt: PropertyChangeEvent => {
      // property changed, apply new value to valueProperty
      val value = propertyClazz.value[Any](property(), name)
      if (valueProperty() != null) {
        valueProperty := value
      }
    }
  }
  // Manage valueProperty -> property
  val valuePropertyListener = valueProperty.listeners.synchronous {
    case evt: PropertyChangeEvent => {
      // value property changed, apply new value to property
      val value = valueProperty()
      val currentValue = propertyClazz.value[Any](property(), name)
      if (value != currentValue) {
        val updated = propertyClazz.modify[T](property(), name, value)
        property := updated
      }
    }
  }

  /**
   * Updates the valueProperty to reflect the value contained in property
   */
  def updateValueProperty(): Unit = {
    property.fireChanged()    // Update the value property
  }

  /**
   * Updates the property to reflect the value contained in valueProperty
   */
  def updateProperty(): Unit = {
    valueProperty.fireChanged()
  }

  /**
   * Disconnects the listeners and stops this binding having any further affect
   */
  def disconnect(): Unit = {
    property.listeners -= propertyListener
    valueProperty.listeners -= valuePropertyListener
  }
}
