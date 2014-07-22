package org.powerscala.json.convert

import org.json4s._
import org.powerscala.Priority
import org.powerscala.json.JSON
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CaseClassSupport extends JSONConverter[AnyRef, JObject] {
  val ClassKey = "jsonClass"

  private var map = Map.empty[Class[_], CaseClassConverter]

  override def toJSON(v: AnyRef) = converter(v.getClass).toJSON(v)

  override def fromJSON(v: JObject) = {
    val jsonClassString = v.obj.collectFirst {
      case (key, value) if key == ClassKey => value.asInstanceOf[JString].s
    }.getOrElse(throw new RuntimeException(s"Unable find '$ClassKey' in JObject ($v)."))
    val jsonClass = Class.forName(jsonClassString)
    converter(jsonClass).fromJSON(v)
  }

  def converter(clazz: Class[_]) = synchronized {
    map.get(clazz) match {
      case Some(converter) => converter
      case None => {
        if (!clazz.isCase) throw new RuntimeException(s"$clazz is not a case class!")
        val converter = new CaseClassConverter(clazz)
        map += clazz -> converter
        converter
      }
    }
  }

  def init() = {
    JSON.add(CaseClassSupport, (value: AnyRef) => value.getClass.isCase, (value: JObject) => value.obj.find(t => t._1 == ClassKey).nonEmpty, Priority.Low)
  }
}

class CaseClassConverter(clazz: EnhancedClass) extends JSONConverter[AnyRef, JObject] {
  val caseValues = clazz.caseValues
  val caseValueMap = caseValues.map(cv => cv.name -> cv).toMap

  override def toJSON(v: AnyRef) = {
    val list = caseValues.map(cv => cv.name -> JSON.parseAndGet(cv[Any](v)))
    val values = if (JSON.writeExtras) {
      CaseClassSupport.ClassKey -> JString(clazz.name) :: list
    } else {
      list
    }
    JObject(values: _*)
  }

  override def fromJSON(v: JObject) = {
    val map = v.obj.collect {
      case (key, value) if caseValueMap.contains(key) => {
        val cv = caseValueMap(key)
        key -> JSON.readAs[Any](value)(Manifest.classType[Any](cv.valueType.javaClass)).getOrElse(throw new RuntimeException(s"Unable to read ${cv.name} as ${cv.valueType} with $v."))
      }
    }.toMap
    clazz.create[AnyRef](map)
  }
}