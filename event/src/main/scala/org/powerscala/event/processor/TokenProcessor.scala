package org.powerscala.event.processor

import org.powerscala.event.{EventState, Listenable}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class TokenProcessor(val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[EventToken]) extends EventProcessor[EventToken, Unit, Unit] {
  protected def handleListenerResponse(value: Unit, state: EventState[EventToken]) = {}

  protected def responseFor(state: EventState[EventToken]) = {}
}

class EventToken private()

object EventToken extends EventToken