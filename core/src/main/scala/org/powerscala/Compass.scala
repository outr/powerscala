package org.powerscala

/**
 * Compass represents the points on a compass as an enum.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed class Compass extends EnumEntry[Compass]

object Compass extends Enumerated[Compass] {
  val North = new Compass
  val South = new Compass
  val East = new Compass
  val West = new Compass
}