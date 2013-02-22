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
    if (manifest != null && manifest.erasure.hasType(classOf[Jsonify])) {
      val empty = manifest.erasure.create[Jsonify](Map.empty)
      val instance = empty.parse(content)
      instance.asInstanceOf[T]
    } else {
      try {
        JSON.parseFull(content) match {
          case Some(value) => parseJSON[T](value)(manifest)
          case None => throw new NullPointerException("Unsupported JSON data: [%s]".format(content))
        }
      } catch {
        case t: Throwable => throw new RuntimeException("Unable to parse (%s): %s".format(manifest, content), t)
      }
    }
  }

  def parseJSON[T](json: Any)(implicit manifest: Manifest[T]): T = json match {
    case map: Map[_, _] => {
      var args = map.asInstanceOf[Map[String, Any]]
      val c: EnhancedClass = if (args.contains("class")) {
        Class.forName(args("class").asInstanceOf[String])
      } else if (args.contains("clazz")) {
        Class.forName(args("clazz").asInstanceOf[String])
      } else {
        manifest.erasure
      }
      c.caseValues.foreach {
        case cv => if (cv.valueType.isCase && args.contains(cv.name)) {
          args += cv.name -> parseJSON[Any](args(cv.name))(Manifest.classType[Any](cv.valueType.javaClass))
        } else if (cv.valueType == classOf[List[_]]) {
          args += cv.name -> parseJSON[Any](args(cv.name))(Manifest.classType[Any](classOf[List[_]]))
        }
      }
      c.create[T](args)
    }
    case list: List[_] => {
      list.map(v => parseJSON[Any](v)(null)).asInstanceOf[T]
    }
    case _ => json.asInstanceOf[T]
//    case _ => throw new RuntimeException("Unsupported: %s (%s)".format(json, manifest))
  }

  def generate(value: Any, specifyClassName: Boolean = true): String = value match {
    case null => null
    case jsonify: Jsonify => jsonify.generate()
    case json: JSONType => formatter(value)
    case _ => {
      val result = generateJSON(value, specifyClassName)
      if (result == value) {
        throw new RuntimeException("Unable to convert: %s (%s)".format(value, value.asInstanceOf[AnyRef].getClass))
      }
      generate(result)
    }
  }

  def generateJSON(value: Any, specifyClassName: Boolean = true): Any = {
    if (value == null) {
      null
    } else if (value.asInstanceOf[AnyRef].getClass.isArray) {
      JSONArray(value.asInstanceOf[Array[_]].toList.map(v => generateJSON(v)))
    } else if (value.isInstanceOf[Seq[_]]) {
      JSONArray(value.asInstanceOf[Seq[_]].toList.map(v => generateJSON(v)))
    } else if (value.asInstanceOf[AnyRef].getClass.isCase) {
      val c: EnhancedClass = value.asInstanceOf[AnyRef].getClass
      val map = c.caseValues.map(cv => cv.name -> generateJSON(cv[AnyRef](value.asInstanceOf[AnyRef]))).toMap match {
        case m if (specifyClassName) => m + ("class" -> c.javaClass.getName)
        case m => m
      }
      JSONObject(map)
    } else {
      value
    }
  }
}