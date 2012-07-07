package org.powerscala.convert

import org.powerscala.reflect._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object CaseClassDisassembler {
  def disassemble(cc: AnyRef) = {
    val clazz: EnhancedClass = cc.getClass
    val values = clazz.caseValues.map(cv => new CaseClassValue(cv, cv(cc)))
    new CaseClass(clazz, values)
  }
}

class CaseClass(val clazz: EnhancedClass, val values: List[CaseClassValue])

class CaseClassValue(val caseValue: CaseValue, val value: Any)