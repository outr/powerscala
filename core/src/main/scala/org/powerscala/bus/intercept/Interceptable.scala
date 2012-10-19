package org.powerscala.bus.intercept

import org.powerscala.bus._
import scala.Some
import org.powerscala.Priority
import annotation.tailrec

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class Interceptable[T](name: String, parent: Interceptable[T] = null)(implicit manifest: Manifest[T]) {
  private var intercepts = List.empty[Intercept]

  def apply(intercept: PartialFunction[T, Any], priority: Priority = Priority.Normal) = synchronized {
    intercepts = (Intercept(intercept.orElse {
      case _ => // Ignore
    }, priority) :: intercepts).sortBy(i => i.priority.value).reverse
  }

  def fire(t: T): Option[T] = {
    val option = if (parent != null) {
      parent.fire(t)
    } else {
      Some(t)
    }

    option match {
      case Some(value) => processLocal(value, intercepts)
      case None => None
    }
  }

  @tailrec
  private def processLocal(t: T, intercepts: List[Intercept]): Option[T] = {
    if (intercepts.isEmpty) {
      Some(t)
    } else {
      val i = intercepts.head
      i.process(t) match {
        case Routing.Stop => None
        case response: RoutingResponse => Some(response.response.asInstanceOf[T])
        case results: RoutingResults => throw new RuntimeException("RoutingResults not supported in Interceptable!")
        case Routing.Continue => processLocal(t, intercepts.tail)
      }
    }
  }

  case class Intercept(f: T => Any, priority: Priority) {
    def process(message: T) = {
      f(message) match {
        case routing: Routing => routing
        case _ => Routing.Continue
      }
    }
  }
}

//class InterceptWrapper[T](val name: String, val value: T)

//case class InterceptNode[T](name: String, intercept: T => Any, priority: Priority)(implicit val manifest: Manifest[InterceptWrapper[T]]) extends TypedNode[InterceptWrapper[T]] {
//  def process(message: InterceptWrapper[T]) = if (message.name == name) {
//    intercept(message.value) match {
//      case routing: Routing => routing
//      case _ => Routing.Continue
//    }
//  } else {
//    Routing.Continue
//  }
//}