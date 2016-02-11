package org.powerscala

import enumeratum._

/**
 * Compass represents the points on a compass as an enum.
 */
sealed abstract class Compass extends EnumEntry

object Compass extends Enum[Compass] {
  case object North extends Compass
  case object South extends Compass
  case object East extends Compass
  case object West extends Compass

  val values = findValues.toVector
}