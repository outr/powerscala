package org.powerscala.json

import org.json4s.JsonAST.{JDecimal, JInt}
import org.json4s.native.JsonMethods

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSONParser {
  private val IntRegex = """(\d+)""".r
  private val DoubleRegex = """(\d*)[.](\d+)""".r

  def apply(s: String) = s match {
    case IntRegex(i) => JInt(i.toInt)
    case DoubleRegex(d) => JDecimal(BigDecimal(d))
    case _ => JsonMethods.parse(s)
  }
}
