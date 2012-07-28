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
  private var _modified = false
  backing.setValue(default)

  /**
   * Modified represents whether the value has been updated since the default value was assigned or revert was called.
   */
  def modified = _modified

  /**
   * Reverts back to the default value and resets the status of "modified".
   */
  def revert() = {
    apply(default)
    _modified = false
  }

  def apply(v: T) = {
    val oldValue = backing.getValue
    val newValue = change(oldValue, v)
    if (oldValue != newValue) {
      backing.setValue(newValue)
      fire(PropertyChangeEvent(this, oldValue, newValue))
    }
    _modified = true
  }

  def apply() = backing.getValue

  def onChange(f: => Unit) = listeners.synchronous {
    case evt: ChangeEvent => f
  }

  def readOnly: Property[T] = this

  override def toString() = "Property[%s](%s)".format(name, value)
}











