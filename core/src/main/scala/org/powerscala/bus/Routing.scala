package org.powerscala.bus

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * Routing is used as a response from Node to determine how the Bus should proceed with processing of the message.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Routing(val continuing: Boolean) extends EnumEntry

object Routing extends Enumerated[Routing] {
  /**
   * Returned by a Node to allow the message to continue processing along to Bus.
   */
  val Continue = new Routing(true)

  /**
   * Returned by a Node to stop processing by additional Nodes.
   */
  val Stop = new Routing(false)

  /**
   * Returned by a Node to stop processing and return the supplied response value.
   */
  def Response(response: Any) = new RoutingResponse(response)

  /**
   * Returned by a Node to represent results. Results works like a Response except continues processing and allows the
   * adding up of additional results from more Nodes while continuing to process the message.
   */
  def Results(results: List[Any]) = new RoutingResults(results)
}

class RoutingResponse private[bus](val response: Any) extends Routing(false) {
  override lazy val name = "Response"
}

class RoutingResults private[bus](val results: List[Any]) extends Routing(true) {
  override lazy val name = "Results"

  override def equals(obj: Any) = obj match {
    case other: RoutingResults => results == other.results
    case _ => false
  }
}