package org.powerscala.property

import backing._


/**
 * NumericProperty adds additional convenience methods for dealing with properties that are numeric.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class NumericProperty(name: String, default: Double, backing: Backing[Double])(implicit override val parent: PropertyParent) extends StandardProperty[Double](name, default, backing) {
  def +=(value: Double) = apply(this.value + value)

  def -=(value: Double) = apply(this.value - value)

  def *=(value: Double) = apply(this.value * value)

  def /=(value: Double) = apply(this.value * value)
}

object NumericProperty {
  /**
   * Creates a new StandardProperty with VariableBacking and the value supplied.
   */
  def apply(name: String, value: Double, backing: Backing[Double] = new VariableBacking[Double])(implicit parent: PropertyParent): NumericProperty = {
    new NumericProperty(name, value, backing)(parent)
  }

  /**
   * Creates a new Property with a value tied to the function supplied.
   */
  def function(_name: String, f: => Double) = new Property[Double] {
    def name = _name

    def apply() = f
  }

  /**
   * Creates a new StandardProperty with VolatileVariableBacking.
   */
  def volatile(name: String, value: Double)(implicit parent: PropertyParent) = apply(name, value, new VolatileVariableBacking[Double])(parent)

  /**
   * Creates a new StandardProperty with AtomicBacking.
   */
  def atomic(name: String, value: Double)(implicit parent: PropertyParent) = apply(name, value, new AtomicBacking[Double])(parent)

  /**
   * Creates a new StandardProperty with LocalBacking.
   */
  def local(name: String, value: Double)(implicit parent: PropertyParent) = apply(name, value, new LocalBacking[Double])(parent)
}