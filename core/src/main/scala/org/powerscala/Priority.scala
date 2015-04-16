package org.powerscala

/**
 * Priority represents a linear prioritization.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Priority(val value: Double) {
  private val stepSize = 0.5

  def lowerBy(steps: Int) = Priority(value - (steps * stepSize))
  def higherBy(steps: Int) = Priority(value + (steps * stepSize))

  override def equals(obj: Any) = obj match {
    case p: Priority => p.value == value
    case _ => false
  }
}

object Priority {
  case object Lowest extends Priority(0.0)
  case object Low extends Priority(0.5)
  case object Normal extends Priority(1.0)
  case object High extends Priority(100.0)
  case object Critical extends Priority(Double.MaxValue)

  val values = Vector(Lowest, Low, Normal, High, Critical)

  def apply(value: Double) = {
    values.find(p => p.value == value) match {
      case Some(p) => p
      case None => new Priority(value)
    }
  }
}