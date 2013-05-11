package org.powerscala.event.processor

import org.powerscala.event.EventState

/**
 * @author Matt Hicks <matt@outr.com>
 */
class OptionProcessor[E, R] extends EventProcessor[E, Option[R], Option[R]] {
  protected def handleListenerResponse(value: Option[R], state: EventState[E]) = if (value.nonEmpty) {
    state.update("optionalResponse", value)
    state.stopPropagation()
  }

  protected def responseFor(state: EventState[E]) = state.getOrElse[Option[R]]("optionalResponse", None)
}