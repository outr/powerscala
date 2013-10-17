package org.powerscala.event.processor

import org.powerscala.event.{EventState, Listenable}

/**
 * ModifiableProcessor takes in a value and allows each listener the option to replace the value as it goes through the
 * process finally returning the result.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class ModifiableProcessor[E](val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, E, E] {
  protected def handleListenerResponse(value: E, state: EventState[E]) = state.event = value

  protected def responseFor(state: EventState[E]) = state.event
}