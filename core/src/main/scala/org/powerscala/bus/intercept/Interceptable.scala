package org.powerscala.bus.intercept

import org.powerscala.bus._
import scala.Some
import org.powerscala.Priority

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class Interceptable[T](name: String, bus: Bus = Bus())(implicit manifest: Manifest[T]) {
  def intercept(intercept: Intercept[T], priority: Priority = Priority.Normal) = {
    bus.add(InterceptNode[T](name, intercept, priority))
  }

  def apply(t: T) = bus(new InterceptWrapper(name, t)) match {
    case Routing.Continue => Some(t)
    case Routing.Stop => None
    case response: RoutingResponse => Some(response.response.asInstanceOf[T])
    case results: RoutingResults => throw new RuntimeException("RoutingResults not supported in Interceptable!")
  }
}

class InterceptWrapper[T](val name: String, val value: T)

case class InterceptNode[T](name: String, intercept: Intercept[T], priority: Priority)(implicit val manifest: Manifest[InterceptWrapper[T]]) extends TypedNode[InterceptWrapper[T]] {
  def process(message: InterceptWrapper[T]) = if (message.name == name) {
    intercept.intercept(message.value)
  } else {
    Routing.Continue
  }
}