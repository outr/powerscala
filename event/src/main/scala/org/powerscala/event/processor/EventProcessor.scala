package org.powerscala.event.processor

import scala.annotation.tailrec
import org.powerscala.event._
import org.powerscala.event.FunctionalListener

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EventProcessor[E, V, R] {
  protected def handleListenerResponse(value: V, state: EventState[E]): Unit
  protected def responseFor(state: EventState[E]): R

  def add(listenable: Listenable, mode: ListenMode = ListenMode.Standard)(f: E => V) = {
    val listener = FunctionalListener(f)
    val wrapper = ListenerWrapper[E, V, R](mode, this, listener)
    listenable.listeners += wrapper
    wrapper
  }

  def remove(listenable: Listenable)(wrapper: ListenerWrapper[E, V, R]) = listenable.listeners -= wrapper

  def fire(event: E, listenable: Listenable, mode: ListenMode = ListenMode.Standard): R = {
    val state = new EventState[E](event, mode, listenable, EventState.current)
    EventState.current = state
    try {
      fireInternal(state, listenable)
    } finally {
      if (EventState.current == state) {
        if (state.causedBy != null) {
          EventState.current = state.causedBy
        } else {
          EventState.clear()
        }
      }
    }
  }

  protected def fireInternal(state: EventState[E], listenable: Listenable): R = {
    fireRecursive(state, listenable.listeners())
    responseFor(state)
  }

  @tailrec
  private def fireRecursive(state: EventState[E], wrappers: List[ListenerWrapper[_, _, _]]): Unit = {
    if (wrappers.nonEmpty && !state.isStopPropagation) {
      val wrapper = wrappers.head
      if (wrapper.processor == this) {
        val listener = wrapper.listener.asInstanceOf[Listener[E, V]]
        val value = listener.receive(state.event)
        handleListenerResponse(value, state)
      }
      fireRecursive(state, wrappers.tail)
    }
  }
}