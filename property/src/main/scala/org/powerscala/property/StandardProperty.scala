package org.powerscala.property

import backing.{VariableBacking, Backing}
import event.PropertyChangeEvent
import org.powerscala.ChangeInterceptor
import org.powerscala.bind.Bindable
import org.powerscala.event.{ChangeEvent, Listenable}

/**
 * StandardProperty is the default implementation of mutable properties with change listening and
 * interception.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class StandardProperty[T](val name: String, val default: T, backing: Backing[T] = new VariableBacking[T])
                         (implicit override val parent: PropertyParent)
                                    extends MutableProperty[T]
                                    with Listenable
                                    with ChangeInterceptor[T]
                                    with Bindable[T]
                                    with Default[T] {
  backing.setValue(default)

  def apply(v: T) = {
    val oldValue = backing.getValue
    val newValue = change(oldValue, v)
    if (oldValue != newValue) {
      backing.setValue(newValue)
      fire(PropertyChangeEvent(this, oldValue, newValue))
    }
  }

  def apply() = backing.getValue

  def onChange(f: => Unit) = listeners.synchronous {
    case evt: ChangeEvent => f
  }

  def readOnly: Property[T] = this

  override def toString() = "Property[%s](%s)".format(name, value)
}











