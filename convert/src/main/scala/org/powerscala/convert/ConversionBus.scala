package org.powerscala.convert

import org.powerscala.bus.{RoutingResponse, Routing, Bus}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class ConversionBus extends Bus {
  add(CaseClassConverter)

  def convert(ref: Any): Any = {
    receive(this, ref) match {
      case Routing.Continue => ref                                                // No changes, finished processing
      case response: RoutingResponse => convert(response.response)                // Modified, keep going
    }
  }
}
