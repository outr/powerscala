package org.powerscala.event

import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FunctionalListener[E, V](f: E => V, priority: Priority = Priority.Normal) extends Listener[E, V] {
  def receive(event: E) = f(event)
}
