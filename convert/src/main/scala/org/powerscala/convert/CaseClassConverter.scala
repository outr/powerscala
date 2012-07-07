package org.powerscala.convert

import org.powerscala.bus.{Bus, Routing, Node}
import org.powerscala.Priority

import org.powerscala.reflect._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object CaseClassConverter extends Node {
  val priority = Priority.Normal

  def receive(bus: Bus, message: Any) = {
    val conversion = bus.asInstanceOf[ConversionBus]
    message match {
      case ref: AnyRef if (ref.getClass.isCase) => {
        val caseClass = CaseClassDisassembler.disassemble(ref)
        val updated = new CaseClass(caseClass.clazz, caseClass.values.map(ccv => new CaseClassValue(ccv.caseValue, conversion.convert(ccv.value))))
        Routing.Response(updated)
      }
      case _ => Routing.Continue
    }
  }
}
