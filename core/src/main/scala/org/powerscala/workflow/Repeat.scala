package org.powerscala.workflow

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed trait Repeat extends EnumEntry

object Repeat extends Enumerated[Repeat] {
  case object All extends Repeat
  case object First extends Repeat
  case object Last extends Repeat

  val values = findValues.toVector
}