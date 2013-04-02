package org.powerscala.bus

import org.powerscala.Priority

/**
 * Node instances may be added to the Bus to process incoming messages to the Bus.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Node {
  /**
   * The Priority of this Node. Default sorting on the Bus takes the priority into account when determining which Node
   * receives incoming messages first.
   */
  def priority: Priority

  /**
   * Called by the Bus when an incoming message occurs.
   */
  def receive(bus: Bus, message: Any): Routing
}

object Node {
  private val fallthrough: PartialFunction[Any, Any] = {
    case _ => // Fall through
  }

  /**
   * Convenience method for creating a Node with a partial function.
   *
   * If the function returns Routing.Stop the Node will stop the message, otherwise Routing.Continue will be implied.
   */
  def apply(priority: Priority = Priority.Normal)(f: PartialFunction[Any, Any]) = {
    FunctionalNode(f.orElse(fallthrough), priority)
  }
}

trait TypedNode[T] extends Node {
  def manifest: Manifest[T]

  def receive(bus: Bus, message: Any) = {
    if (manifest.runtimeClass.isAssignableFrom(message.asInstanceOf[AnyRef].getClass)) {
      process(message.asInstanceOf[T])
    } else {
      Routing.Continue
    }
  }

  def process(message: T): Routing
}

case class FunctionalNode[T](f: (T) => Any, priority: Priority)(implicit val manifest: Manifest[T]) extends TypedNode[T] {
  def process(message: T) = f(message) match {
    case Routing.Stop => Routing.Stop
    case response: RoutingResponse => response
    case results: RoutingResults => results
    case _ => Routing.Continue
  }
}