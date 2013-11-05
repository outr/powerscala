package org.powerscala.event.processor

import org.powerscala.event.{Listenable, EventState}

/**
 * UnitProcessor takes in E and passes it to all listeners. The response is irrelevant as Unit is the return value.
 * This is useful for standard event processing that has no result but simply passes events to listeners.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class UnitProcessor[E](val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, Unit, Unit] {
  protected def handleListenerResponse(value: Unit, state: EventState[E]) = {}

  protected def responseFor(state: EventState[E]) = {}
}