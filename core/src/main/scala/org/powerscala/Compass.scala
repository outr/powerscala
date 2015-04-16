package org.powerscala

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * Compass represents the points on a compass as an enum.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed class Compass extends EnumEntry

object Compass extends Enumerated[Compass] {
  case object North extends Compass
  case object South extends Compass
  case object East extends Compass
  case object West extends Compass

  val values = findValues.toVector
}