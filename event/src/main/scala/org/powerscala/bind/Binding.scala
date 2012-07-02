package org.powerscala.bind

import org.powerscala.event._

/**
 * Represents a listener to see changes and reflect them back to <code>binded</code> when they occur.
 *
 * This is used directly by Binding and should probably not be used directly.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Binding[T](binded: Function1[T, Any], val acceptFilter: Event => Boolean) extends Listener {
  def apply(event: Event) = event match {
    case changeEvent: ChangeEvent => binded(changeEvent.newValue.asInstanceOf[T])
    case _ => // Ignore everything else
  }
}