package org.powerscala.event

import org.powerscala.Priority
import org.powerscala.bus.{Routing, TypedNode}

/**
 *
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 * Date: 12/2/11
 */
trait Listener extends TypedNode[Event] with Function1[Event, Any] {
  val manifest = Listener.EventManifest

  def apply(event: Event): Any

  def priority = Priority.Normal

  def acceptFilter: Event => Boolean

  final def process(event: Event) = if (acceptFilter(event)) {
    apply(event) match {
      case routing: Routing => routing
      case _ => Routing.Continue
    }
  } else {
    Routing.Continue
  }
}

case class TargetFilter(target: Listenable) extends Function1[Event, Boolean] {
  def apply(event: Event) = event.target == target
}

object Listener {
  val EventManifest = Manifest.classType[Event](classOf[Event])

  protected[event] def withFallthrough[T](f: PartialFunction[T, Any]) = f.orElse[T, Any] {
    case _ => // Fall through
  }
}