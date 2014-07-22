package org.powerscala.json.convert

import org.json4s._
import org.powerscala.json.JSON

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait JSONConverter[T, J <: JValue] {
  def toJSON(v: T): J
  def fromJSON(v: J): T
}

object BooleanSupport extends JSONConverter[Boolean, JBool] {
  override def toJSON(v: Boolean) = JBool(v)
  override def fromJSON(v: JBool) = v.value
}

object IntSupport extends JSONConverter[Int, JInt] {
  override def toJSON(v: Int) = JInt(v)
  override def fromJSON(v: JInt) = v.num.intValue()
}

object BigIntSupport extends JSONConverter[BigInt, JInt] {
  override def toJSON(v: BigInt) = JInt(v)
  override def fromJSON(v: JInt) = v.num
}

object DoubleSupport extends JSONConverter[Double, JDouble] {
  override def toJSON(v: Double) = JDouble(v)
  override def fromJSON(v: JDouble) = v.num
}

object DecimalSupport extends JSONConverter[BigDecimal, JDecimal] {
  override def toJSON(v: BigDecimal) = JDecimal(v)
  override def fromJSON(v: JDecimal) = v.num
}

object StringSupport extends JSONConverter[String, JString] {
  override def toJSON(v: String) = JString(v)
  override def fromJSON(v: JString) = v.s
}

object ListSupport extends JSONConverter[List[_], JArray] {
  override def toJSON(v: List[_]) = JArray(v.toList.map(JSON.parseAndGet))
  override def fromJSON(v: JArray) = v.arr.map(JSON.readAndGet[Any])
}

object MapSupport extends JSONConverter[Map[String, _], JObject] {
  override def toJSON(v: Map[String, _]) = JObject(v.map(t => JField(t._1, JSON.parseAndGet(t._2))).toList)
  override def fromJSON(v: JObject) = v.obj.map(t => t._1 -> JSON.readAndGet(t._2)).toMap
}

object OptionSupport extends JSONConverter[Option[_], JObject] {
  override def toJSON(v: Option[_]) = {
    JObject("option" -> (if (v.nonEmpty) JSON.parseAndGet(v.get) else JNull))
  }
  override def fromJSON(v: JObject) = {
    val option = v.obj.find(t => t._1 == "option").get._2
    Option(JSON.readAndGet(option))
  }

  def init() = {
    JSON.add(this, jsonMatcher = (obj: JObject) => obj.obj.size == 1 && obj.obj.head._1 == "option")
  }
}