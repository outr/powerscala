package org.powerscala.json

import org.json4s.JsonAST.{JString, JDecimal, JInt}
import org.json4s.native.JsonMethods

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSONParser {
  private val IntRegex = """(\d+)""".r
  private val DoubleRegex = """(\d*)[.](\d+)""".r
  private val StringRegex = """["](.*)["]""".r

  def apply(s: String) = s match {
    case IntRegex(i) => JInt(i.toInt)
    case DoubleRegex(d) => JDecimal(BigDecimal(d))
    case StringRegex(s) => JString(s)
    case _ => JsonMethods.parse(s)
  }
}
