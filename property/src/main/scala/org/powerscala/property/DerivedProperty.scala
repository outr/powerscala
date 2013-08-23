package org.powerscala.property

/**
 * DerivedProperty allows a property to be created that relies on and even defines the contents of another property.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait DerivedProperty[T, O] extends Property[T] {
  change.on {
    case evt => other := fromT(evt.newValue)
  }
  other.change.on {
    case evt => value = fromO(evt.newValue)
  }

  def other: Property[O]
  def fromT(value: T): O
  def fromO(value: O): T
}
