package org.powerscala.json

import org.json4s.JsonAST.{JString, JObject}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object TypedSupport {
  private var classes = Map.empty[Class[_], String]
  private var classNames = Map.empty[String, String]
  private var aliases = Map.empty[String, Class[_]]

  init()

  def register(alias: String, classes: Class[_]*) = synchronized {
    classes.foreach {
      case c => {
        this.classes += c -> alias
        this.classNames += c.getName -> alias
        this.aliases += alias -> c
      }
    }
  }

  private def init() = {
    j2o.partial(None) {
      case m: Map[_, _] => {
        val map = m.asInstanceOf[Map[String, Any]]
        if (map.contains("type") && aliases.contains(map("type").asInstanceOf[String])) {
          val updated = map.map(t => if (t._1 == "type") "class" -> aliases(map("type").asInstanceOf[String]).getName else t)
          Some(updated)
        } else {
          None
        }
      }
    }
    o2j.partial(None) {
      case j: JObject => {
        val classNameOption = j.obj.collectFirst {
          case t if t._1 == "class" => t._2.asInstanceOf[JString].s
        }
        classNameOption match {
          case Some(className) if classNames.contains(className) => {
            val alias = classNames(className)
            Some(JObject(j.obj.map(t => if (t._1 == "class") "type" -> JString(alias) else t): _*))
          }
          case _ => None
        }
      }
    }
  }
}