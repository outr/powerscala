package org.powerscala.event

import org.powerscala.Storage

/**
 * @author Matt Hicks <matt@outr.com>
 */
class EventState[E] protected[event](val event: E,
                                      val mode: ListenMode,
                                      val listenable: Listenable,
                                      val causedBy: EventState[_]) extends Storage[Any] {
  def isStopPropagation = getOrElse[Boolean]("stopPropagation", false)
  def stopPropagation() = update("stopPropagation", true)
}

object EventState {
  private val _current = new ThreadLocal[EventState[_]]

  def current = _current.get()
  def current_=(state: EventState[_]) = _current.set(state)
  def clear() = _current.remove()
}