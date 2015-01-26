package org.powerscala.json

import org.json4s._
import org.powerscala.enum.{Enumerated, EnumEntry}
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Defaults {
  private val jsonConverter = new TypeEventConverter(j2o)
  private val objectConverter = new TypeEventConverter(o2j)

  def init() = {
    // Option Support
    o2j.partial(None) {
      case o: Option[_] => Some(Map("option" -> o.map(toJSON).getOrElse(JNull)))
    }
    j2o.partial(None) {
      case map: Map[_, _] => {
        val m = map.asInstanceOf[Map[String, Any]]
        if (m.contains("option")) {
          val v = m("option")
          Some(Option(v))
        } else {
          None
        }
      }
    }

    // Enum Support
    o2j.partial(None) {
      case e: EnumEntry => Some(Map("enumClass" -> e.getClass.getName, "name" -> e.name))
    }
    j2o.partial(None) {
      case map: Map[_, _] => {
        val m = map.asInstanceOf[Map[String, String]]
        if (m.get("enumClass").nonEmpty) {
          val enumClass = Class.forName(m("enumClass"))
          val enumerated = enumClass.instance.getOrElse(throw new RuntimeException(s"Unable to find Enumerated for $enumClass.")).asInstanceOf[Enumerated[EnumEntry]]
          Some(enumerated(m("name")))
        } else {
          None
        }
      }
    }

    // Null Support
    o2j.partial(None) {
      case null => Some(JNull)
    }
    j2o.partial(None) {
      case v: JValue if v eq JNull => Some(null)
    }

    byType[JBool, Boolean] {        // Boolean
      case j => j.value
    } {
      case b => JBool(b)
    }.objectAlias(classOf[java.lang.Boolean])

    byType[JInt, Int] {             // Int
      case j => j.num.toInt
    } {
      case i => JInt(i)
    }.objectAlias(classOf[java.lang.Integer])

    byType[JDouble, Double] {       // Double
      case j => j.num
    } {
      case d => JDouble(d)
    }.objectAlias(classOf[java.lang.Double])

    byType[JDecimal, BigDecimal] {  // BigDecimal
      case j => j.num
    } {
      case d => JDecimal(d)
    }

    byType[JString, String] {       // String
      case j => j.s
    } {
      case s => JString(s)
    }

    byType[JArray, List[_]] {       // List
      case j => j.arr.map(fromJSON)
    } {
      case l => JArray(l.map(toJSON))
    }.objectAlias(classOf[::[_]], Nil.getClass)

    MapSupport
  }

  class TypeConversionInstance(fromJSON: Any => Any, toJSON: Any => Any) {
    def jsonAlias(classes: Class[_]*) = {
      classes.foreach(jsonConverter.add(_, fromJSON))
      this
    }

    def objectAlias(classes: Class[_]*) = {
      classes.foreach(objectConverter.add(_, toJSON))
      this
    }
  }
}