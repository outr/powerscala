package org.powerscala.event.processor

import org.powerscala.event.{EventState, Listenable}

/**
 * ModifiableProcessor takes in an event and allows each listener the option to modify and return
 *
 * @author Matt Hicks <matt@outr.com>
 */
class ModifiableOptionProcessor[E](val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, Option[E], Option[E]] {
  protected def handleListenerResponse(value: Option[E], state: EventState[E]) = if (value.isEmpty) {
    state.stopPropagation = true
  } else {
    state.event = value.get
  }

  protected def responseFor(state: EventState[E]) = if (state.stopPropagation) {
    None
  } else {
    Some(state.event)
  }
}