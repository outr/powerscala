package org.powerscala.json

import org.json4s.JsonAST.JString
import org.json4s._
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CaseClassSupport {
  j2o.partial(None) {
    case m: Map[_, _] => {
      val map = m.asInstanceOf[Map[String, Any]]
      if (map.contains("class")) {
        val clazz = Class.forName(map("class").asInstanceOf[String])
        if (clazz.isCase) {
          Some(clazz.create[AnyRef](map))
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