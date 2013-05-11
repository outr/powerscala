package org.powerscala.event

import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Listener[E, V] {
  def priority: Priority

  def receive(event: E): V
}