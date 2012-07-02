package org.powerscala.event

import org.powerscala.Priority

/**
 * ChangeEvent represents a change of value on the target.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait ChangeEvent extends Event {
  def oldValue: Any
  def newValue: Any
}

case class DefaultChangeEvent(oldValue: Any, newValue: Any) extends ChangeEvent

object ChangeEvent {
  private val allFilter = (l: Listenable) => true

  def apply(oldValue: Any, newValue: Any) = DefaultChangeEvent(oldValue, newValue)

  def record(listenable: Listenable, sideffects: Boolean = false, depth: Int = Int.MaxValue, filter: Listenable => Boolean = allFilter)(action: => Any) = {
    val currentCause = Event.current
    var list = List.empty[Change]
    val listener = listenable.listeners.filter.descendant(depth).priority(Priority.High).synchronous {
      case evt: ChangeEvent => {
        if (sideffects || evt.cause == currentCause) {
          val change = list.find(c => c.listenable == evt.target) match {
            case Some(c) => {
              list = list.filterNot(i => i == c)    // Remove existing entry
              c.copy(newValue = evt.newValue)       // Duplicate with new value
            }
            case None => Change(evt.target, evt.oldValue, evt.newValue)   // Create a new change
          }
          list = change :: list
        }
      }
    }
    try {
      action
    } finally {
      listenable.listeners -= listener
    }
    list.reverse
  }
}

case class Change(listenable: Listenable, oldValue: Any, newValue: Any)