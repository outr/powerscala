package org.powerscala.event.processor

import org.powerscala.event.{Intercept, EventState}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class InterceptProcessor[E] extends EventProcessor[E, Intercept, Intercept] {
  protected def handleListenerResponse(value: Intercept, state: EventState[E]) = if (value == Intercept.Stop) {
    state.stopPropagation()
  }

  protected def responseFor(state: EventState[E]) = if (state.isStopPropagation) {
    Intercept.Stop
  } else {
    Intercept.Continue
  }
}