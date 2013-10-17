package org.powerscala.event

import org.powerscala.Priority
import org.powerscala.event.processor.EventProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class FunctionalListener[Event, Response](f: Event => Response,
                                               name: String,
                                               priority: Priority = Priority.Normal,
                                               modes: List[ListenMode] = EventProcessor.DefaultModes)
                                              (implicit manifest: Manifest[Event]) extends Listener[Event, Response] {
  def eventClass = manifest.runtimeClass.asInstanceOf[Class[Event]]

  def receive(event: Event) = f(event)
}
