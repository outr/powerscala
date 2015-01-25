package org.powerscala.event.processor

import scala.annotation.tailrec
import org.powerscala.event._
import org.powerscala.reflect.EnhancedClass

import language.existentials
import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EventProcessor[Event, Response, Result] {
  def name: String
  def listenable: Listenable
  def eventManifest: Manifest[Event]
  protected def handleListenerResponse(value: Response, state: EventState[Event]): Unit
  protected def responseFor(state: EventState[Event]): Result

  if (listenable == null) {
    throw new NullPointerException("Listenable cannot be null!")
  }

  def +=(listener: Listener[Event, Response]) = add(listener)

  def -=(listener: Listener[Event, Response]) = remove(listener)

  /**
   * Creates the listener but does not add it.
   *
   * @param priority the priority of this listener
   * @param modes the modes the listener should listen to. If this is empty it will be set to EventProcessor.DefaultModes.
   * @param f the function to invoke when the listener is invoked.
   * @return Listener
   */
  def create(priority: Priority, modes: ListenMode*)(f: Event => Response) = {
    val modesList = if (modes.isEmpty) {
      EventProcessor.DefaultModes
    } else {
      modes.toList
    }
    FunctionalListener(f, name, priority, modesList)(eventManifest)
  }

  def listen(priority: Priority, modes: ListenMode*)(f: Event => Response) = {
    val listener = create(priority, modes: _*)(f)
    this += listener
    listener
  }

  def and[NE >: Event, NV >: Response, NR >: Result](processor: EventProcessor[NE, NV, NR]): ProcessorGroup[NE, NV, NR] = {
    new ProcessorGroup(List(processor, this.asInstanceOf[EventProcessor[NE, NV, NR]]))
  }

  def on(f: Event => Response, priority: Priority = Priority.Normal) = listen(priority)(f)

  def partial(default: Response, priority: Priority = Priority.Normal)(f: PartialFunction[Event, Response]) = listen(priority)((evt: Event) => f.applyOrElse(evt, (e: Event) => default))

  /**
   * Invokes the function upon each event until it returns Some[V] and then removes the listener from receiving any
   * other invocations.
   *
   * @param default the default value to send if None is returned by the function.
   * @param priority the priority for this listener. Defaults to Normal.
   * @param f the function to invoke upon event.
   * @return Listener[E, V, R]
   */
  def onceConditional(default: Response, priority: Priority = Priority.Normal)(f: Event => Option[Response]) = {
    var listener: Listener[Event, Response] = null
    val function = (e: Event) => f(e) match {
      case Some(v) => {
        listenable.listeners -= listener
        v
      }
      case None => default
    }
    listener = listen(priority)(function)

    listener
  }

  /**
   * Works similarly to <code>on</code> but after the first event is received the listener is removed.
   *
   * @param f the function to invoke upon event.
   * @return listener
   */
  def once(f: Event => Response, priority: Priority = Priority.Normal) = {
    var listener: Listener[Event, Response] = null
    val function = (e: Event) => {
      listenable.listeners -= listener
      f(e)
    }
    listener = listen(priority)(function)

    listener
  }

  def add(listener: Listener[Event, Response]) = listenable.listeners += listener

  def remove(listener: Listener[Event, Response]) = listenable.listeners -= listener

  def fire(event: Event, mode: ListenMode = ListenMode.Standard): Result = {
    EventState.around(event, listenable) {
      case state => {
        fireInternal(state, mode, listenable)
        responseFor(state)
      }
    }
  }

  protected def fireInternal(state: EventState[Event], mode: ListenMode, listenable: Listenable): Unit = {
    fireRecursive(state, mode, listenable.listeners())
    if (!state.stopPropagation) {
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
  protected def fireAdditional(state: EventState[Event], mode: ListenMode, listenable: Listenable): Unit = {}

  @tailrec
  private def fireRecursive(state: EventState[Event], mode: ListenMode, listeners: List[Listener[_, _]]): Unit = {
    if (listeners.nonEmpty && !state.stopPropagation) {
      val listener = listeners.head
      if (isNameValid(listener) && isListenerTypeValid(state, listener) && isModeValid(listener, mode)) {
        val l = listener.asInstanceOf[Listener[Event, Response]]
        val value = l.receive(state.event)
        handleListenerResponse(value, state)
      }
      fireRecursive(state, mode, listeners.tail)
    }
  }

  protected def isModeValid(listener: Listener[_, _], mode: ListenMode) = {
    val valid = listener.modes.contains(mode)
    valid
  }

  protected def isNameValid(listener: Listener[_, _]) = {
    val valid = listener.name == name
    valid
  }

  protected def isListenerTypeValid(state: EventState[Event], listener: Listener[_, _]) = if (state.event != null) {
    val listenerEventClass = EnhancedClass.convertPrimitives(listener.eventClass)
    val eventClass = EnhancedClass.convertPrimitives(state.event.getClass)
    val valid = listenerEventClass.isAssignableFrom(eventClass)
    valid
  } else {
    true    // We can't validate by class
  }
}

object EventProcessor {
  val DefaultModes = List(ListenMode.Standard)
}