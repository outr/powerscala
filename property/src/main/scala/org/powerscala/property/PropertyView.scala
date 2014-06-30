package org.powerscala.property

import org.powerscala.concurrent.AtomicBoolean

/**
 * PropertyView wraps around another property to provide a converted view of another type.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait PropertyView[T, Other] extends Property[T] {
  def viewing: Property[Other]
  def convertFrom(value: Other): T
  def convertTo(value: T): Other = {
    throw new UnsupportedOperationException("The convertTo method in PropertyView must be overridden to support " +
                                            "modifying the value back to the property being viewed.")
  }

  private val viewChanging = new AtomicBoolean
  private def tryChange(f: => Unit) = if (viewChanging.compareAndSet(false, true)) {
    try {
      f
    } finally {
      viewChanging.set(false)
    }
  }
  viewing.change.on {                       // Update this property when the value change in the viewing property
    case evt => tryChange {
      value = convertFrom(evt.newValue)
    }
  }
  change.on {                               // Update the viewing property when this property's value changes
    case evt => tryChange {
      viewing := convertTo(evt.newValue)
    }
  }
}
