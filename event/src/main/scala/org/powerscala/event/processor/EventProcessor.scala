package org.powerscala.event.processor

import scala.annotation.tailrec
import org.powerscala.event._
import org.powerscala.reflect.EnhancedClass

import language.existentials
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EventProcessor[E, V, R] extends Logging {
  def name: String
  def listenable: Listenable
  def eventManifest: Manifest[E]
  protected def handleListenerResponse(value: V, state: EventState[E]): Unit
  protected def responseFor(state: EventState[E]): R

  if (listenable == null) {
    throw new NullPointerException("Listenable cannot be null!")
  }

  def listen(modes: ListenMode*)(f: E => V): ListenerWrapper[E, V, R] = {
    listenable.listen(name, modes: _*)(f)(eventManifest)
  }

  def and[NE >: E, NV >: V, NR >: R](processor: EventProcessor[NE, NV, NR]): ProcessorGroup[NE, NV, NR] = {
    new ProcessorGroup(List(processor, this.asInstanceOf[EventProcessor[NE, NV, NR]]))
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
      if (isNameValid(wrapper) && isWrapperTypeValid(state, wrapper) && isModeValid(wrapper, mode)) {
        val listener = wrapper.listener.asInstanceOf[Listener[E, V]]
        val value = listener.receive(state.event)
        handleListenerResponse(value, state)
      }
      fireRecursive(state, mode, wrappers.tail)
    }
  }

  protected def isModeValid(wrapper: ListenerWrapper[_, _, _], mode: ListenMode) = {
    val valid = wrapper.modes.contains(mode)
    if (!valid) {
      debug(s"isModeValid - Modes: ${wrapper.modes}, Mode: $mode")
    }
    valid
  }

  protected def isNameValid(wrapper: ListenerWrapper[_, _, _]) = {
    val valid = wrapper.name == name
    if (!valid) {
      debug(s"isNameValid - ProcessorName: $name / WrapperName: ${wrapper.name}")
    }
    valid
  }

  protected def isWrapperTypeValid(state: EventState[E], wrapper: ListenerWrapper[_, _, _]) = if (state.event != null) {
    val listenerEventClass = EnhancedClass.convertPrimitives(wrapper.eventManifest.runtimeClass)
    val eventClass = EnhancedClass.convertPrimitives(state.event.getClass)
    val valid = listenerEventClass.isAssignableFrom(eventClass)
    if (!valid) {
      debug(s"isWrapperValid: $listenerEventClass not assignable from $eventClass")
    }
    valid
  } else {
    true    // We can't validate by class
  }
}

object EventProcessor {
  val DefaultModes = List(ListenMode.Standard)
}