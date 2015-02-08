package org.powerscala

import org.json4s.JValue
import org.json4s.JsonAST.{JString, JObject}
import org.json4s.native.JsonMethods
import org.powerscala.event.Listenable
import org.powerscala.event.processor.OptionProcessor
import org.powerscala.json.Defaults.TypeConversionInstance
import org.powerscala.reflect._
import scala.annotation.tailrec
import scala.language.implicitConversions

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object json extends Listenable {
  /**
   * Translates objects to JSON values.
   */
  val o2j = new OptionProcessor[Any, Any]("o2j")
  /**
   * Translates JSON values into Scala objects.
   */
  val j2o = new OptionProcessor[Any, Any]("j2o")

  def toJSON(v: Any) = toJSONInternal(v, None)

  def fromJSON(v: JValue) = fromJSONInternal(v, None)

  def typedJSON[Type](v: JValue)(implicit manifest: Manifest[Type]) = v match {
    case o: JObject => fromJSONInternal(o.copy("class" -> JString(manifest.runtimeClass.getName) :: o.obj), None).asInstanceOf[Type]
    case _ => {
      val value = fromJSON(v).asInstanceOf[Type]
      EnhancedMethod.convertTo(null, value, manifest.runtimeClass).asInstanceOf[Type]
    }
  }

  @tailrec
  private def toJSONInternal(v: Any, previous: Option[Any]): JValue = {
    val option = o2j.fire(v)
    if (previous == option || option.isEmpty) {
      if (option.nonEmpty) {
        option.get match {
          case r: JValue => r
          case r => throw new RuntimeException(s"Resulting type is not a JSON type: $r (${r.getClass.getName}).")
        }
      } else if (previous.nonEmpty) {
        previous.get match {
          case r: JValue => r
          case r => throw new RuntimeException(s"Resulting type is not a JSON type: $r (${r.getClass.getName}).")
        }
      } else {
        v match {
          case value: JValue => value
          case _ => throw new RuntimeException(s"Unsupported conversion to JSON: $v (${v.getClass}).")
        }
      }
    } else {
      toJSONInternal(option.get, option)
    }
  }

  @tailrec
  private def fromJSONInternal(v: Any, previous: Option[Any]): Any = {
    val option = j2o.fire(v)
    if (previous == option || option.isEmpty) {
      if (option.nonEmpty) {
        option.get match {
          case r: JValue => throw new RuntimeException(s"Resulting type is still a JSON type: $r (${r.getClass.getName}).")
          case r => r
        }
      } else if (previous.nonEmpty) {
        previous.get match {
          case r: JValue => throw new RuntimeException(s"Resulting type is still a JSON type: $r (${r.getClass.getName}).")
          case r => r
        }
      } else {
        throw new RuntimeException(s"Unsupported conversion from JSON: $v (${v.getClass}).")
      }
    } else {
      fromJSONInternal(option.get, option)
    }
  }

  implicit class RenderableJValue(v: JValue) {
    def fromJSON = org.powerscala.json.fromJSON(v)
    def pretty = JsonMethods.pretty(JsonMethods.render(v))
    def compact = JsonMethods.compact(JsonMethods.render(v))
    def stringify(pretty: Boolean = false) = if (pretty) {
      this.pretty
    } else {
      this.compact
    }
  }

  implicit def string2JValue(s: String): JValue = JSONParser(s)

  def byType[JSONType <: JValue, ObjectType](fromJSON: JSONType => ObjectType)
                                            (toJSON: ObjectType => JSONType)
                                            (implicit jsonTypeManifest: Manifest[JSONType], objectTypeManifest: Manifest[ObjectType]) = {
    val instance = new TypeConversionInstance(fromJSON.asInstanceOf[Any => Any], toJSON.asInstanceOf[Any => Any])
    instance.jsonAlias(jsonTypeManifest.runtimeClass)
    instance.objectAlias(objectTypeManifest.runtimeClass)
    instance
  }

  Defaults.init()
  CaseClassSupport
}