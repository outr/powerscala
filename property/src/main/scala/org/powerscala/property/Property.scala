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

case class PropertyBuilder[T](_name: String = null,
                              _default: Option[T] = None,
                              _backing: Backing[T] = new VariableBacking[T])
                             (implicit _parent: PropertyParent, manifest: Manifest[T]) {
  def name(_name: String) = copy[T](_name = _name)(_parent, manifest)
  def default(_default: T) = copy[T](_default = Some(_default))
  /**
   * Creates a new StandardProperty with VolatileVariableBacking.
   */
  def volatile = copy[T](_backing = new VolatileVariableBacking[T])
  /**
   * Creates a new StandardProperty with AtomicBacking.
   */
  def atomic = copy[T](_backing = new AtomicBacking[T])
  /**
   * Creates a new StandardProperty with LocalBacking.
   */
  def local = copy[T](_backing = new LocalBacking[T])
  def variable = copy[T](_backing = new VariableBacking[T])
  def parent(_parent: PropertyParent) = copy[T]()(_parent = _parent)

  def build() = if (_default.nonEmpty) {
    new StandardProperty[T](_name, _default.get, _backing)(_parent, manifest)
  } else {
    new StandardProperty[T](_name)(_parent, manifest)
  }
}

object Property {
  def apply[T](implicit _parent: PropertyParent = null, manifest: Manifest[T]) = PropertyBuilder[T]()(_parent, manifest)

  def apply[T](name: String, default: T)(implicit _parent: PropertyParent, manifest: Manifest[T]) = {
    new StandardProperty[T](name, default)(_parent, manifest)
  }

  def apply[T](name: String, default: T, backing: Backing[T])(implicit _parent: PropertyParent, manifest: Manifest[T]) = {
    new StandardProperty[T](name, default, backing)(_parent, manifest)
  }

  /**
   * Creates a new Property with a value tied to the function supplied.
   */
  def function[T](_name: String, f: => T) = new Property[T] {
    val name = () => _name

    def apply() = f
  }
}