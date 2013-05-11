package org.powerscala.event

/**
 * @author Matt Hicks <matt@outr.com>
 */
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
  def clear() = synchronized {
    _listeners = Nil
  }
}