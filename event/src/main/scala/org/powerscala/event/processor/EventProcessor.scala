package org.powerscala.event.processor

import scala.annotation.tailrec
import org.powerscala.event._
import org.powerscala.event.FunctionalListener
import org.powerscala.reflect.EnhancedClass

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EventProcessor[E, V, R] {
  def listenable: Listenable
  def eventManifest: Manifest[E]
  protected def handleListenerResponse(value: V, state: EventState[E]): Unit
  protected def responseFor(state: EventState[E]): R

  def listen(modes: ListenMode*)(f: E => V): ListenerWrapper[E, V, R] = {
    val modesList = if (modes.isEmpty) {
      EventProcessor.DefaultModes
    } else {
      modes.toList
    }
    val listener = FunctionalListener(f)
    val wrapper = ListenerWrapper[E, V, R](modesList, this, listener)
    listenable.listeners += wrapper
    wrapper
  }

  def on(f: E => V): ListenerWrapper[E, V, R] = listen()(f)

  def remove(wrapper: ListenerWrapper[E, V, R]) = listenable.listeners -= wrapper

  def fire(event: E, mode: ListenMode = ListenMode.Standard): R = {
    val state = new EventState[E](event, listenable, EventState.current)
    EventState.current = state
    try {
      fireInternal(state, mode, listenable)
      responseFor(state)
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

  protected def fireInternal(state: EventState[E], mode: ListenMode, listenable: Listenable): Unit = {
    fireRecursive(state, mode, listenable.listeners())
    if (!state.isStopPropagation) {
      fireAdditional(state, mode, listenable)
    }
  }

  /**
   * Allows extending classes to continue processing on additional listeners if needed before a response is determined
   * and sent back to the caller.
   *
   * @param state the current EventState
   * @param mode the current ListenMode
   * @param listenable the current Listenable
   */
  protected def fireAdditional(state: EventState[E], mode: ListenMode, listenable: Listenable): Unit = {}

  @tailrec
  private def fireRecursive(state: EventState[E], mode: ListenMode, wrappers: List[ListenerWrapper[_, _, _]]): Unit = {
    if (wrappers.nonEmpty && !state.isStopPropagation) {
      val wrapper = wrappers.head
      if (isWrapperValid(state, wrapper) && wrapper.modes.contains(mode)) {
        val listener = wrapper.listener.asInstanceOf[Listener[E, V]]
        val value = listener.receive(state.event)
        handleListenerResponse(value, state)
      }
      fireRecursive(state, mode, wrappers.tail)
    }
  }

  protected def isWrapperValid(state: EventState[E], wrapper: ListenerWrapper[_, _, _]) = {
    val listenerEventClass = EnhancedClass.convertClass(wrapper.processor.eventManifest.runtimeClass)
    val eventClass = EnhancedClass.convertClass(state.event.getClass)
    listenerEventClass == eventClass
  }
}

object EventProcessor {
  val DefaultModes = List(ListenMode.Standard)
}