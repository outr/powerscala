package org.powerscala.event

import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Listener[Event, Response] {
  def name: String
  def eventClass: Class[Event]
  def modes: List[ListenMode]
  def priority: Priority

  def receive(event: Event): Response
}