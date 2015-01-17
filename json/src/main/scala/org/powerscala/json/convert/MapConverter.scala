package org.powerscala.json.convert

import org.json4s._
import org.powerscala.Priority
import org.powerscala.json.JSON

/**
 * @author Matt Hicks <matt@outr.com>
 */
object MapConverter extends JSONConverter[Map[String, _], JObject] {
  override def toJSON(v: Map[String, _]) = JObject(v.map(t => JField(t._1, JSON.parseAndGet(t._2))).toList)
  override def fromJSON(v: JObject) = v.obj.map(t => t._1 -> JSON.readAndGet(t._2)).toMap

  def init() = {
    JSON.add(MapConverter, jsonMatcher = (value: JObject) => true, priority = Priority.Lowest)
  }
}