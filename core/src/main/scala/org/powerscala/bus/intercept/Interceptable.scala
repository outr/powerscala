package org.powerscala.bus.intercept

import org.powerscala.bus._
import scala.Some
import org.powerscala.Priority

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class Interceptable[T](name: String, bus: Bus = Bus(), parent: Interceptable[T] = null)(implicit manifest: Manifest[T]) {
  def intercept(intercept: PartialFunction[T, Any], priority: Priority = Priority.Normal) = {
    bus.add(InterceptNode[T](name, intercept.orElse {
      case _ =>
    }, priority))
  }

  def apply(t: T): Option[T] = {
    val option = if (parent != null) {
      parent(t)
    } else {
      Some(t)
    }

    option match {
      case Some(value) => {
        bus(new InterceptWrapper(name, t)) match {
          case Routing.Continue => Some(t)
          case Routing.Stop => None
          case response: RoutingResponse => Some(response.response.asInstanceOf[T])
          case results: RoutingResults => throw new RuntimeException("RoutingResults not supported in Interceptable!")
        }
      }
      case None => None
    }
  }
}

class InterceptWrapper[T](val name: String, val value: T)

case class InterceptNode[T](name: String, intercept: T => Any, priority: Priority)(implicit val manifest: Manifest[InterceptWrapper[T]]) extends TypedNode[InterceptWrapper[T]] {
  def process(message: InterceptWrapper[T]) = if (message.name == name) {
    intercept(message.value) match {
      case routing: Routing => routing
      case _ => Routing.Continue
    }
  } else {
    Routing.Continue
  }
}