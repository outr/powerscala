package org.powerscala

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * Priority represents a linear prioritization.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Priority private(val value: Double) extends EnumEntry {
  private val stepSize = 0.5

  def lowerBy(steps: Int) = Priority(value - (steps * stepSize))
  def higherBy(steps: Int) = Priority(value + (steps * stepSize))

  override def equals(obj: Any) = obj match {
    case p: Priority => p.value == value
    case _ => false
  }
}

object Priority extends Enumerated[Priority] {
  val Lowest = new Priority(0.0)
  val Low = new Priority(0.5)
  val Normal = new Priority(1.0)
  val High = new Priority(100.0)
  val Critical = new Priority(Double.MaxValue)

  def apply(value: Double) = {
    values.find(p => p.value == value) match {
      case Some(p) => p
      case None => new Priority(value)
    }
  }
}