package org.powerscala.event.processor

import org.powerscala.event.{Listenable, Intercept, EventState}

/**
 * InterceptProcessor gives each listener the ability to Stop or Continue the processing. If Stop is returned by one of
 * the listeners no further listeners will be invoked with the event and Stop will be returned. If all listeners return
 * Continue or there are no listeners, then Continue will be returned.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class InterceptProcessor[E](val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, Intercept, Intercept] {
  protected def handleListenerResponse(value: Intercept, state: EventState[E]) = if (value == Intercept.Stop) {
    state.stopPropagation()
  }

  protected def responseFor(state: EventState[E]) = if (state.isStopPropagation) {
    Intercept.Stop
  } else {
    Intercept.Continue
  }
}