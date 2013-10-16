package org.powerscala.event

import org.powerscala.MappedStorage

/**
 * @author Matt Hicks <matt@outr.com>
 */
class EventState[E] protected[event](val listenable: Listenable,
                                     val causedBy: EventState[_]) extends MappedStorage[String, Any] {
  protected[event] def this(event: E, listenable: Listenable, causedBy: EventState[_]) = {
    this(listenable, causedBy)
    this.event = event
  }

  def event = apply[E]("event")
  def event_=(evt: E) = update("event", evt)
  def isStopPropagation = getOrElse[Boolean]("stopPropagation", false)
  def stopPropagation() = update("stopPropagation", true)
}

object EventState {
  private val _current = new ThreadLocal[EventState[_]]

  def current = _current.get()
  def current_=(state: EventState[_]) = _current.set(state)
  def clear() = _current.remove()
}