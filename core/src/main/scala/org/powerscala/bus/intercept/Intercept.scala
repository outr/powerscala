package org.powerscala.bus.intercept

import org.powerscala.bus.Routing

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Intercept[T] {
  def intercept(t: T): Routing

  def continue() = Routing.Continue

  def reject() = Routing.Stop

  def replace(t: T) = Routing.Response(t)
}