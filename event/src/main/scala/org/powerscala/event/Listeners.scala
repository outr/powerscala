package org.powerscala.event

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Listeners {
  private var _listeners = List.empty[Listener[_, _]]

  def apply() = _listeners
  def +=(listener: Listener[_, _]) = synchronized {
    if (listener.modes.isEmpty) {
      throw new RuntimeException(s"ListenModes cannot be empty for: $listener")
    }
    _listeners = (listener :: _listeners.reverse).reverse   // Use natural ordering before we sort
    _listeners = _listeners.sortBy(l => -l.priority.value)   // Sort based on priority
  }
  def -=(listener: Listener[_, _]) = synchronized {
    _listeners = _listeners.filterNot(l => l == listener)
  }
  def clear() = synchronized {
    _listeners = Nil
  }
}