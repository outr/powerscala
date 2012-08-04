package org.powerscala.property

import backing._
import org.powerscala.hierarchy.{Named, Child}
import org.powerscala.naming.NamingParent

/**
 * Property represents an object containing a value.
 */
trait Property[T] extends Function0[T] with Child with Named {
  def parent: PropertyParent = null

  parent match {
    case p: NamingParent => p.add(this)
    case _ => // Missed
  }

  /**
   * Retrieves the value of the property.
   */
  def value = apply()
}

object Property {
  /**
   * Creates a new StandardProperty with VariableBacking and the value supplied.
   */
  def apply[T](name: String, value: T, backing: Backing[T] = new VariableBacking[T])(implicit parent: PropertyParent) = {
    new StandardProperty[T](name, value, backing)(parent)
  }

  /**
   * Creates a new Property with a value tied to the function supplied.
   */
  def function[T](_name: String, f: => T) = new Property[T] {
    val name = () => _name

    def apply() = f
  }

  /**
   * Creates a new StandardProperty with VolatileVariableBacking.
   */
  def volatile[T](name: String, value: T)(implicit parent: PropertyParent) = apply(name, value, new VolatileVariableBacking[T])(parent)

  /**
   * Creates a new StandardProperty with AtomicBacking.
   */
  def atomic[T](name: String, value: T)(implicit parent: PropertyParent) = apply(name, value, new AtomicBacking[T])(parent)

  /**
   * Creates a new StandardProperty with LocalBacking.
   */
  def local[T](name: String, value: T)(implicit parent: PropertyParent) = apply(name, value, new LocalBacking[T])(parent)
}