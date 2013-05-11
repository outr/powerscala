package org.powerscala.event.processor

import org.powerscala.event.{Listenable, EventState}

/**
 * ListProcessor expects an Option[R] from each listener and builds a list from Some[R] responses. The combined List[R]
 * is returned upon completion of iteration over listeners or at stopPropagation.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class ListProcessor[E, R](implicit val listenable: Listenable, val eventManifest: Manifest[E]) extends EventProcessor[E, Option[R], List[R]] {
  val token = "listResponse"

  protected def handleListenerResponse(value: Option[R], state: EventState[E]) = value match {
    case Some(v) => {
      val list = state.getOrElse[List[R]](token, Nil)
      state(token) = (v :: list.reverse).reverse
    }
    case None => // Nothing to add to the list
  }

  protected def responseFor(state: EventState[E]) = state.getOrElse[List[R]](token, Nil)
}