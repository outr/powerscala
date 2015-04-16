package org.powerscala.event

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
  * Intercept represents the intercept response for a listener on an InterceptEventBus.
  *
  * @author Matt Hicks <matt@outr.com>
  */
sealed trait Intercept extends EnumEntry

object Intercept extends Enumerated[Intercept] {
  case object Continue extends Intercept
  case object Stop extends Intercept

  val values = findValues.toVector
}