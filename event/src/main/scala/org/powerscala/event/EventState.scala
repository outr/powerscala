package org.powerscala.event

import org.powerscala.{LocalStack, MapStorage}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class EventState[E] private(@volatile var event: E, val listenable: Listenable, val causedBy: Option[EventState[_]]) {
  @volatile var stopPropagation = false
  lazy val store = new MapStorage[String, Any]()
}

object EventState {
  private val states = new LocalStack[EventState[_]]

  def currentOption = states.get()

  def current = currentOption.getOrElse(throw new RuntimeException("No current state is defined."))

  def around[E, R](event: E, listenable: Listenable)(f: EventState[E] => R): R = {
    val state = new EventState(event, listenable, states.get())
    states.push(state)
    try {
      f(state)
    } finally {
      states.pop(state)
    }
  }
}