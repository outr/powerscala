package org.powerscala.bus

import annotation.tailrec
import org.powerscala.ref.{ReferenceType, Reference}
import org.powerscala.Priority
import collection.mutable.ListBuffer

/**
 * Bus represents a global location for receiving and handling messaging. Nodes can be added in order to process
 * messages and sorting is determined by priorities by default.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Bus(val priority: Priority = Priority.Normal) extends Node {
  /**
   * Sorting method for ordering of nodes. This defaults to sorting on the Priority associated with the Nodes.
   *
   * No sorting is applied if this is set to null.
   */
  var sort = prioritySort

  private var nodes: List[Reference[Node]] = Nil

  /**
   * Adds the referenced Node to the Bus. Will return null if the Node already exists in the Bus and will not be added
   * again.
   *
   * The ReferenceType determines how this will be referenced internally to avoid hanging on to references for objects
   * that otherwise would be available for garbage collection. The ReferenceType defaults to Hard meaning it will never
   * be garbage collected.
   */
  def add(node: Node, referenceType: ReferenceType = ReferenceType.Hard) = synchronized {
    if (nodes.contains(node)) {
      null
    } else {
      nodes = (referenceType(node) :: nodes.reverse).reverse // TODO: determine a faster mechanism
      if (sort != null) {
        nodes = nodes.sortWith(referenceSort)
      }
      node
    }
  }

  /**
   * Removes the referenced Node from the Bus.
   */
  def remove(node: Node) = synchronized {
    nodes = nodes.filterNot(n => n == node)
    node
  }

  /**
   * Removes a node based on the wrapping reference.
   */
  def remove(ref: Reference[Node]) = synchronized {
    nodes = nodes.filterNot(n => n == ref)
    ref
  }

  /**
   * Injects a message to this Bus for processing by the associated Nodes.
   *
   * @return Routing defining how this was processed by the nodes.
   */
  def apply(message: Any) = receive(this, message)

  def receive(bus: Bus, message: Any) = process(message, nodes, null)

  @tailrec
  private def process(message: Any, nodes: List[Reference[Node]], buffer: ListBuffer[Any]): Routing = {
    if (nodes.nonEmpty) {
      val ref = nodes.head
      val node = ref.getOrNull
      if (node != null) {
        node.receive(this, message) match {
          case Routing.Stop if (buffer == null) => Routing.Stop
          case Routing.Stop => Routing.Results(buffer.toList)
          case response: RoutingResponse => response
          case results: RoutingResults => {
            val b = buffer match {
              case null => new ListBuffer[Any]()
              case _ => buffer
            }
            b ++= results.results
            process(message, nodes.tail, b)
          }
          case Routing.Continue => process(message, nodes.tail, buffer)
        }
      } else {
        remove(ref)
        if (buffer != null) {
          Routing.Results(buffer.toList)
        } else {
          Routing.Continue
        }
      }
    } else {
      if (buffer != null) {
        Routing.Results(buffer.toList)
      } else {
        Routing.Continue
      }
    }
  }

  /**
   * true if there are no Nodes attached to this Bus.
   */
  def isEmpty = nodes.isEmpty

  /**
   * true if there are Nodes attached to this Bus.
   */
  def nonEmpty = nodes.nonEmpty

  /**
   * The number of Nodes attached to this Bus.
   */
  def length = nodes.length

  private val referenceSort = (r1: Reference[Node], r2: Reference[Node]) => {
    val n1 = r1.getOrNull
    val n2 = r2.getOrNull
    if (n1 == null) {
      true
    } else if (n2 == null) {
      false
    } else {
      sort(n1, n2)
    }
  }

  private lazy val prioritySort = (n1: Node, n2: Node) => n1.priority.value > n2.priority.value
}

/**
 * Default Bus and primary pipeline through which most messages pass.
 */
object Bus extends Bus(Priority.Normal)