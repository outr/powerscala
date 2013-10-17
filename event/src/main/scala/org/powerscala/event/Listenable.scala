package org.powerscala.event

import org.powerscala.event.processor.EventProcessor
import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Listenable {
  implicit val thisListenable = this

  val listeners = new Listeners

  def listen[Event, Response, Result](name: String,
                                      priority: Priority,
                                      modes: ListenMode*)(f: Event => Response)(implicit eventManifest: Manifest[Event]) = {
    val modesList = if (modes.isEmpty) {
      EventProcessor.DefaultModes
    } else {
      modes.toList
    }
    val listener = FunctionalListener(f, name, priority, modesList)
    listeners += listener
    listener
  }
}

object Listenable {
  def listenTo[E, V, R](name: String, priority: Priority, listenables: Listenable*)(modes: ListenMode*)(f: E => V)(implicit eventManifest: Manifest[E]) = {
    listenables.foreach {
      case listenable => listenable.listen[E, V, R](name, priority, modes: _*)(f)
    }
  }
}