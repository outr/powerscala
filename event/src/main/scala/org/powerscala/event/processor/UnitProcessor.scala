package org.powerscala.event.processor

import org.powerscala.event.EventState

/**
 * @author Matt Hicks <matt@outr.com>
 */
class UnitProcessor[E] extends EventProcessor[E, Unit, Unit] {
  protected def handleListenerResponse(value: Unit, state: EventState[E]) = {}

  protected def responseFor(state: EventState[E]) = {}
}