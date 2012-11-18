package org.powerscala.json

import util.parsing.json._
import org.powerscala.reflect._
import util.parsing.json.JSONObject

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSONConverter {
  val formatter: Any => String = (value: Any) => value match {
    case null => "null"
    case s : String => "\"" + JSONFormat.quoteString(s) + "\""
    case jo : JSONObject => jo.toString(formatter)
    case ja : JSONArray => ja.toString(formatter)
    case other => other.toString
  }

  def parse[T](content: String)(implicit manifest: Manifest[T]) = {
    JSON.parseFull(content) match {
      case Some(value) => parseJSON[T](value)(manifest)
      case None => throw new NullPointerException("Unsupported JSON data: %s".format(content))
    }
  }

  def parseJSON[T](json: Any)(implicit manifest: Manifest[T]): T = json match {
    case map: Map[_, _] => {
      var args = map.asInstanceOf[Map[String, Any]]
      val c: EnhancedClass = manifest.erasure
      c.caseValues.foreach {
        case cv => if (cv.valueType.isCase && args.contains(cv.name)) {
          args += cv.name -> parseJSON[Any](args(cv.name))(Manifest.classType[Any](cv.valueType.javaClass))
        }
      }
      c.create[T](args)
    }
    case _ => throw new RuntimeException("Unsupported: %s".format(json))
  }

  def generate(value: Any): String = value match {
    case null => null
    case json: JSONType => formatter(value)
    case _ => generate(generateJSON(value))
  }

  def generateJSON(value: Any): Any = if (value == null) {
    null
  } else if (value.asInstanceOf[AnyRef].getClass.isCase) {
    val c: EnhancedClass = value.asInstanceOf[AnyRef].getClass
    val map = c.caseValues.map(cv => cv.name -> generateJSON(cv[AnyRef](value.asInstanceOf[AnyRef]))).toMap
    JSONObject(map)
  } else {
    value
  }
}