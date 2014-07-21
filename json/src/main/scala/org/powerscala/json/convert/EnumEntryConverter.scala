package org.powerscala.json.convert

import org.json4s._
import org.powerscala.Priority
import org.powerscala.enum.{Enumerated, EnumEntry}
import org.powerscala.json.JSON
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object EnumEntryConverter extends JSONConverter[EnumEntry, JObject] {
  val ClassKey = "enumClass"

  override def toJSON(v: EnumEntry) = JObject(ClassKey -> JString(v.getClass.getName), "name" -> JString(v.name))

  override def fromJSON(v: JObject) = {
    val classString = v.obj.collectFirst {
      case (key, value) if key == ClassKey => value.asInstanceOf[JString].s
    }.getOrElse(throw new RuntimeException(s"Unable find '$ClassKey' in JObject ($v)."))
    val name = v.obj.collectFirst {
      case (key, value) if key == "name" => value.asInstanceOf[JString].s
    }.getOrElse(throw new RuntimeException(s"Unable find 'name' in JObject ($v)."))
    val enumClass = Class.forName(classString)
    val enumerated = enumClass.instance.getOrElse(throw new RuntimeException(s"Unable to find Enumerated for $enumClass.")).asInstanceOf[Enumerated[EnumEntry]]
    enumerated(name)
  }

  def init() = {
    JSON.add(EnumEntryConverter, jsonMatcher = (value: JObject) => value.obj.find(t => t._1 == ClassKey).nonEmpty, priority = Priority.Low)
  }
}
