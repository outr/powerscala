package org.powerscala.convert.json

import util.parsing.json.JSONFormat

import org.powerscala.reflect._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class JSONConverter {
  def apply(value: Any) = value match {
    case null => null
    case s: String => quotedString(s)
//    case seq: Seq[_] => convertSequence
    case v: AnyRef if (v.getClass.isCase) => convertCaseClass(v)
  }

  def quotedString(s: String) = "\"" + JSONFormat.quoteString(s) + "\""

  def convertCaseClass(v: AnyRef) = {

  }
}
