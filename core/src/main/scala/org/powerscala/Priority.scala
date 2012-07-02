package org.powerscala

/**
 * Priority represents a linear prioritization.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class Priority(value: Double) extends EnumEntry[Priority]

object Priority extends Enumerated[Priority] {
  val Lowest = Priority(0.0)
  val Low = Priority(0.5)
  val Normal = Priority(1.0)
  val High = Priority(100.0)
  val Critical = Priority(Double.MaxValue)
}