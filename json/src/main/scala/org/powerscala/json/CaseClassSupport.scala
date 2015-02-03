package org.powerscala.json

import org.json4s.JsonAST.JString
import org.json4s._
import org.powerscala.LocalStack
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CaseClassSupport {
  private var pathMapping = Map.empty[String, Class[_]]

  def registerPath(clazz: Class[_], key: String, subClass: Class[_]) = synchronized {
    pathMapping += s"${clazz.getName}.$key" -> subClass
  }

  j2o.partial(None) {
    case m: Map[_, _] => {
      val map = m.asInstanceOf[Map[String, Any]]
      if (map.contains("class")) {
        val clazz = Class.forName(map("class").asInstanceOf[String])
        if (clazz.isCase) {
          val updated = map.map {
            case (key, value) => {
              val path = s"${clazz.getName}.$key"
              val newValue = if (pathMapping.contains(path)) {
                value match {
                  case l: List[_] => l.map {
                    case m: Map[_, _] => j2o.fire(m.asInstanceOf[Map[String, Any]] + ("class" -> pathMapping(path).getName)).get
                  }
                  case m: Map[_, _] => j2o.fire(m.asInstanceOf[Map[String, Any]] + ("class" -> pathMapping(path).getName)).get
                  case _ => throw new RuntimeException(s"Unsupported pathMapping type: $value.")
                }
              } else {
                value
              }
              key -> newValue
            }
          }
          Some(clazz.create[AnyRef](updated))
        } else {
          None
        }
      } else {
        None
      }
    }
  }
  o2j.partial(None) {
    case o: AnyRef if o.getClass.isCase && !o.isInstanceOf[JValue] => {
      Some(("class" -> JString(o.getClass.getName) :: o.getClass.caseValues.map(cv => cv.name -> toJSON(cv[Any](o)))).toMap)
    }
  }
}