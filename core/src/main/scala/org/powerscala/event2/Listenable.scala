package org.powerscala.event2

import org.powerscala.{Priority, Storage}
import scala.annotation.tailrec
import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Listenable {
  val listeners = new Listeners
}

class Listeners {
  private var _listeners = List.empty[ListenerWrapper[_, _, _]]

  def apply() = _listeners
  def +=(wrapper: ListenerWrapper[_, _, _]) = synchronized {
    _listeners = (wrapper :: _listeners.reverse).reverse   // Use natural ordering before we sort
    _listeners = _listeners.sortBy(l => l.listener.priority.value)   // Sort based on priority
  }
  def -=(wrapper: ListenerWrapper[_, _, _]) = synchronized {
    _listeners = _listeners.filterNot(w => w == wrapper)
  }
}

trait Listener[E, V] {
  def priority: Priority

  def receive(event: E): V
}

case class FunctionalListener[E, V](f: E => V, priority: Priority = Priority.Normal) extends Listener[E, V] {
  def receive(event: E) = f(event)
}

case class ListenerWrapper[E, V, R](mode: ListenMode, processor: EventProcessor[E, V, R], listener: Listener[E, V])

class ListenMode

object ListenMode {
  val Standard = new ListenMode
}

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
    val state = new EventState[E](event, mode)
    fireInternal(state, listenable)
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
        fireRecursive(state, wrappers.tail)
      }
    }
  }
}

/**
 * ListProcessor expects an Option[R] from each listener and builds a list from Some[R] responses. The combined List[R]
 * is returned upon completion of iteration over listeners or at stopPropagation.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class ListProcessor[E, R] extends EventProcessor[E, Option[R], List[R]] {
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

class OptionalProcessor[E, R] extends EventProcessor[E, Option[R], Option[R]] {
  protected def handleListenerResponse(value: Option[R], state: EventState[E]) = if (value.nonEmpty) {
    state.update("optionalResponse", value)
    state.stopPropagation()
  }

  protected def responseFor(state: EventState[E]) = state.getOrElse[Option[R]]("optionalResponse", None)
}

class UnitProcessor[E] extends EventProcessor[E, Unit, Unit] {
  protected def handleListenerResponse(value: Unit, state: EventState[E]) = {}

  protected def responseFor(state: EventState[E]) = {}
}

class InterceptProcessor[E] extends EventProcessor[E, Intercept, Intercept] {
  protected def handleListenerResponse(value: Intercept, state: EventState[E]) = if (value == Intercept.Stop) {
    state.stopPropagation()
  }

  protected def responseFor(state: EventState[E]) = if (state.isStopPropagation) {
    Intercept.Stop
  } else {
    Intercept.Continue
  }
}

/**
 * Intercept represents the intercept response for a listener on an InterceptEventBus.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class Intercept protected() extends EnumEntry

object Intercept extends Enumerated[Intercept] {
  val Continue = new Intercept
  val Stop = new Intercept
}

class EventState[E] protected[event2](val event: E, val mode: ListenMode) extends Storage[Any] {
  def isStopPropagation = getOrElse[Boolean]("stopPropagation", false)
  def stopPropagation() = update("stopPropagation", true)
}