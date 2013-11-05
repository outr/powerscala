package org.powerscala.event.processor

import org.powerscala.event.{EventState, Listenable}

/**
 * TokenProcessor works similarly to UnitProcessor except there is no real "event" that is supplied so EventToken is
 * provided. This can be useful for events that simply say "something happened" but don't include any additional
 * information that would be provided in an event.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class TokenProcessor(val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[EventToken]) extends EventProcessor[EventToken, Unit, Unit] {
  protected def handleListenerResponse(value: Unit, state: EventState[EventToken]) = {}

  protected def responseFor(state: EventState[EventToken]) = {}
}

class EventToken private()

object EventToken extends EventToken