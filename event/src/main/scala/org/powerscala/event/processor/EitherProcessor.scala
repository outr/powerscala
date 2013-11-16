package org.powerscala.event.processor

import org.powerscala.event.{EventState, Listenable}

/**
 * EitherProcessor takes in an event and allows each listener the option to modify and return via Left or respond with
 * an alternate response with Right (immediately stops propagating and returns).
 *
 * @author Matt Hicks <matt@outr.com>
 */
class EitherProcessor[E, R](val name: String)(implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, Either[E, R], Either[E, R]] {
  private val processorToken = "other"

  protected def handleListenerResponse(value: Either[E, R], state: EventState[E]) = value match {
    case Left(e) => state.event = e
    case Right(r) => {
      state(processorToken) = r
      state.stopPropagation()
    }
  }

  protected def responseFor(state: EventState[E]) = if (state.isStopPropagation && state.contains(processorToken)) {
    Right(state[R](processorToken))
  } else {
    Left(state.event)
  }
}