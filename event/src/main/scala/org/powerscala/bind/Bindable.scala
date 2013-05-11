package org.powerscala.bind

import org.powerscala.event.processor.UnitProcessor
import org.powerscala.event.{ListenerWrapper, Listenable, Change}

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Bindable[T] extends ((T) => Unit) with Listenable {
  def change: UnitProcessor[Change[T]]

  def bind(bindable: Bindable[T]) = {
    bindable.change.on {
      case c => apply(c.newValue)
    }
  }

  def bindTo[S](bindable: Bindable[S])(implicit converter: S => T) = {
    bindable.change.on {
      case c => apply(converter(c.newValue))
    }
  }

  def unbind(listener: ListenerWrapper[Change[_], Unit, Unit]) = listeners -= listener
}