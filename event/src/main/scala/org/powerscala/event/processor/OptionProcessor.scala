package org.powerscala.event.processor

import org.powerscala.event.{Listenable, EventState}

/**
 * OptionProcessor gives each listener the ability to return Option[R]. If Some is returned no more listeners will be
 * invoked for the event and it will return immediately. If all listeners return None or there are no listeners, then
 * None will be the result.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class OptionProcessor[E, R](val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, Option[R], Option[R]] {
  protected def handleListenerResponse(value: Option[R], state: EventState[E]) = if (value.nonEmpty) {
    state.store.update("optionalResponse", value)
    state.stopPropagation = true
  }

  protected def responseFor(state: EventState[E]) = state.store.getOrElse[Option[R]]("optionalResponse", None)
}