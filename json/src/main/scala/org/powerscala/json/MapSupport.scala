package org.powerscala.json

import org.json4s._
import org.powerscala.StringUtil
import org.powerscala.event.Listenable
import org.powerscala.event.processor.{ModifiableProcessor, OptionProcessor}

import scala.collection.immutable.HashMap.HashTrieMap

/**
 * @author Matt Hicks <matt@outr.com>
 */
object MapSupport extends Listenable {
  val o2j = new MapModifiableProcessor("o2j")
  val j2o = new MapModifiableProcessor("j2o")

  byType[JObject, Map[_, _]] {    // Map
    case j => j2o.fire(j.obj.map(t => j2oKey(t._1) -> fromJSON(t._2)).toMap)
  } {
    case m => JObject(o2j.fire(m.asInstanceOf[Map[String, Any]]).toList.map(t => t._1.toString -> toJSON(t._2)))
  }.objectAlias(classOf[Map.Map1[_, _]], classOf[Map.Map2[_, _]], classOf[Map.Map3[_, _]], classOf[Map.Map4[_, _]], classOf[HashTrieMap[_, _]])

  private def j2oKey(key: String) = StringUtil.toCamelCase(key)
}

class MapModifiableProcessor(name: String)(implicit listenable: Listenable) extends ModifiableProcessor[Map[String, Any]](name)(listenable, implicitly[Manifest[Map[String, Any]]]) {
  def removeWhen(key: String, value: JValue) = on {
    case m if m.get(key) == Some(value) => m - key
    case m => m
  }

  /**
   * Excludes the 'class' output during JSON output.
   */
  def excludeClass[C](implicit manifest: Manifest[C]) = removeWhen("class", JString(manifest.runtimeClass.getName))
}