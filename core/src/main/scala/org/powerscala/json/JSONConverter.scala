package org.powerscala.json

import util.parsing.json._
import org.powerscala.reflect._
import util.parsing.json.JSONObject

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSONConverter {
  private var typeMap = Map.empty[String, EnhancedClass]

  def registerType(hierarchicalName: String, clazz: EnhancedClass) = synchronized {
    typeMap += hierarchicalName -> clazz
  }

  val formatter: Any => String = (value: Any) => value match {
    case null => "null"
    case s : String => "\"" + JSONFormat.quoteString(s) + "\""
    case jo : JSONObject => jo.toString(formatter)
    case ja : JSONArray => ja.toString(formatter)
    case other => other.toString
  }

  def parse[T](content: String)(implicit manifest: Manifest[T]) = {
    if (manifest != null && manifest.runtimeClass.hasType(classOf[Jsonify])) {
      val empty = manifest.runtimeClass.create[Jsonify](Map.empty)
      val instance = empty.parse(content)
      instance.asInstanceOf[T]
    } else if (content == "") {
      null.asInstanceOf[T]
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

  def parseJSON[T](json: Any, hierarchicalName: String = "")(implicit manifest: Manifest[T]): T = json match {
    case map: Map[_, _] => {
      var args = map.asInstanceOf[Map[String, Any]]
      val c: EnhancedClass = if (args.contains("class")) {
        Class.forName(args("class").asInstanceOf[String])
      } else if (args.contains("clazz")) {
        Class.forName(args("clazz").asInstanceOf[String])
      } else if (manifest != null) {
        manifest.runtimeClass
      } else {
        typeMap.get(hierarchicalName) match {
          case Some(clazz) => clazz
          case None => throw new RuntimeException(s"No hierarchical class association for: $hierarchicalName - $map")
        }
      }
      c.caseValues.foreach {
        case cv => if (cv.valueType.isCase && args.contains(cv.name)) {
          args += cv.name -> parseJSON[Any](args(cv.name), s"${c.name}.${cv.name}")(Manifest.classType[Any](cv.valueType.javaClass))
        } else if (cv.valueType == classOf[List[_]]) {
          args += cv.name -> parseJSON[Any](args(cv.name), s"${c.name}.${cv.name}")(Manifest.classType[Any](classOf[List[_]]))
        } else if (cv.valueType == classOf[Map[_, _]]) {
          val mapped = args(cv.name).asInstanceOf[Map[_, _]].map {
            case (key, value) => {
              val updatedKey = parseJSON[Any](key, s"${c.name}.${cv.name}.key")(null)
              val updatedValue = parseJSON[Any](value, s"${c.name}.${cv.name}.value")(null)
              updatedKey -> updatedValue
            }
          }
          args += cv.name -> mapped
        }
      }
      c.create[T](args)
    }
    case list: List[_] => {
//      val clazz = typeMap.getOrElse(hierarchicalName, throw new RuntimeException(s"No hierarchical class association for: $hierarchicalName - $list"))
      list.map(v => parseJSON[Any](v, hierarchicalName)(null)).asInstanceOf[T]
    }
//    case _ => json.asInstanceOf[T]
    case _ => {
      val c: EnhancedClass = typeMap.get(hierarchicalName) match {
        case Some(clazz) => clazz
        case None => throw new RuntimeException(s"No hierarchical class association for: $hierarchicalName - $json")
      }
      EnhancedMethod.convertTo(hierarchicalName, json, c).asInstanceOf[T]
//      throw new RuntimeException("Unsupported: %s (%s)".format(json, manifest))
    }
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
    } else if (value.isInstanceOf[Jsonify]) {
      value.asInstanceOf[Jsonify].generate()
    } else if (value.asInstanceOf[AnyRef].getClass.isArray) {
      JSONArray(value.asInstanceOf[Array[_]].toList.map(v => generateJSON(v, specifyClassName = specifyClassName)))
    } else if (value.isInstanceOf[Seq[_]]) {
      JSONArray(value.asInstanceOf[Seq[_]].toList.map(v => generateJSON(v, specifyClassName = specifyClassName)))
    } else if (value.asInstanceOf[AnyRef].getClass.isCase) {
      val c: EnhancedClass = value.asInstanceOf[AnyRef].getClass
      val map = c.caseValues.map(cv => cv.name -> generateJSON(cv[AnyRef](value.asInstanceOf[AnyRef]), specifyClassName = specifyClassName)).toMap match {
        case m if specifyClassName => m + ("class" -> c.javaClass.getName)
        case m => m
      }
      JSONObject(map)
    } else if (value.isInstanceOf[Map[_, _]]) {
      JSONObject(value.asInstanceOf[Map[String, Any]])
    } else {
      value
    }
  }
}