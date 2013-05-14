package org.powerscala.event

import org.powerscala.event.processor.EventProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Listenable {
  implicit val thisListenable = this

  val listeners = new Listeners

  def listen[E, V, R](name: String, modes: ListenMode*)(f: E => V)(implicit eventManifest: Manifest[E]): ListenerWrapper[E, V, R] = {
    val modesList = if (modes.isEmpty) {
      EventProcessor.DefaultModes
    } else {
      modes.toList
    }
    val listener = FunctionalListener(f)
    val wrapper = ListenerWrapper[E, V, R](name, modesList, listener)
    listeners += wrapper
    wrapper
  }
}

object Listenable {
  def listenTo[E, V, R](name: String, listenables: Listenable*)(modes: ListenMode*)(f: E => V)(implicit eventManifest: Manifest[E]) = {
    listenables.foreach {
      case listenable => listenable.listen[E, V, R](name, modes: _*)(f)
    }
  }
}