package org.powerscala.event

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
  * Intercept represents the intercept response for a listener on an InterceptEventBus.
  *
  * @author Matt Hicks <matt@outr.com>
  */
class Intercept protected() extends EnumEntry

object Intercept extends Enumerated[Intercept] {
  val Continue = new Intercept
  val Stop = new Intercept
}
