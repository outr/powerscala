package org.powerscala.json

import org.json4s._
import org.powerscala.event.Listenable
import org.powerscala.event.processor.{ModifiableProcessor, OptionProcessor}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object MapSupport extends Listenable {
  val o2j = new ModifiableProcessor[Map[String, Any]]("o2j")
  val j2o = new ModifiableProcessor[Map[String, Any]]("j2o")

  byType[JObject, Map[_, _]] {    // Map
    case j => j2o.fire(j.obj.map(t => t._1 -> fromJSON(t._2)).toMap)
  } {
    case m => JObject(o2j.fire(m.asInstanceOf[Map[String, Any]]).toList.map(t => t._1.toString -> toJSON(t._2)))
  }.objectAlias(classOf[Map.Map1[_, _]], classOf[Map.Map2[_, _]], classOf[Map.Map3[_, _]], classOf[Map.Map4[_, _]])
}
