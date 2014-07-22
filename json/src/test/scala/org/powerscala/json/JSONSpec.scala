package org.powerscala.json

import org.json4s._
import org.powerscala.Language
import org.powerscala.json.convert.{EnumEntryConverter, CaseClassSupport}
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JSONSpec extends WordSpec with Matchers {
  "JSON" when {
    "reading standard types" should {
      "handle Boolean" in {
        JSON.readAndGet[Boolean](JBool(value = true)) should equal(true)
      }
      "handle Int" in {
        JSON.readAndGet[Int](JInt(5)) should equal(5)
      }
      "handle Double" in {
        JSON.readAndGet[Double](JDouble(5.2)) should equal(5.2)
      }
      "handle Decimal" in {
        JSON.readAndGet[BigDecimal](JDecimal(5.3)) should equal(5.3)
      }
      "handle String" in {
        JSON.readAndGet[String](JString("Hello")) should equal("Hello")
      }
      "handle List" in {
        JSON.readAndGet[List[_]](JArray(List(JString("Hello"), JInt(3), JDouble(1.2)))) should equal(List("Hello", 3, 1.2))
      }
      "handle Map" in {
        JSON.readAndGet[Map[String, _]](JObject("First" -> JInt(1), "Second" -> JDecimal(2.3), "Three" -> JString("Third"), "Four" -> JBool(value = false))) should equal(Map("First" -> 1, "Second" -> 2.3, "Three" -> "Third", "Four" -> false))
      }
    }
    "parsing standard types" should {
      "handle Boolean" in {
        val result = JSON.parse(true)
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JBool])
        val b = v.asInstanceOf[JBool]
        b.value should equal(true)
      }
      "handle Int" in {
        val result = JSON.parse(5)
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JInt])
        val i = v.asInstanceOf[JInt]
        i.num.intValue() should equal(5)
      }
      "handle Double" in {
        val result = JSON.parse(5.2)
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JDouble])
        val i = v.asInstanceOf[JDouble]
        i.num should equal(5.2)
      }
      "handle Decimal" in {
        val result = JSON.parse(BigDecimal(5.2))
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JDecimal])
        val i = v.asInstanceOf[JDecimal]
        i.num should equal(5.2)
      }
      "handle String" in {
        val result = JSON.parse("Hello")
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JString])
        val s = v.asInstanceOf[JString]
        s.s should equal("Hello")
      }
      "handle List" in {
        val result = JSON.parse(List(1, BigDecimal(2.2), "Hello", false))
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JArray])
        val a = v.asInstanceOf[JArray]
        val list = a.arr
        list.size should equal(4)
        list(0) should equal(JInt(1))
        list(1) should equal(JDecimal(BigDecimal(2.2)))
        list(2) should equal(JString("Hello"))
        list(3) should equal(JBool(value = false))
      }
      "handle Map" in {
        val result = JSON.parse(Map("First" -> 1, "Second" -> BigDecimal(2.3), "Third" -> "Goodbye", "Fourth" -> true))
        result shouldNot equal(None)
        val v = result.get
        v.getClass should equal(classOf[JObject])
        val o = v.asInstanceOf[JObject]
        val map = o.obj.toMap
        map("First") should equal(JInt(1))
        map("Second") should equal(JDecimal(2.3))
        map("Third") should equal(JString("Goodbye"))
        map("Fourth") should equal(JBool(value = true))
      }
    }
    "handling custom types" should {
      "properly read EnumEntry" in {
        JSON.readAndGet[Language](JObject(EnumEntryConverter.ClassKey -> JString(classOf[Language].getName), "name" -> JString(Language.German.name))) should equal(Language.German)
      }
      "properly read EnumEntry without class info" in {
        JSON.readAs[Language](JObject("name" -> JString(Language.Bemba.name))) should equal(Some(Language.Bemba))
      }
      "properly parse EnumEntry" in {
        JSON.parseAndGet(Language.Albanian) should equal(JObject(EnumEntryConverter.ClassKey -> JString(classOf[Language].getName), "name" -> JString(Language.Albanian.name)))
      }
      "properly parse EnumEntry without writing class info" in {
        JSON.dontWriteExtras {
          JSON.parseAndGet(Language.Albanian) should equal(JObject("name" -> JString(Language.Albanian.name)))
        }
      }
    }
    "reading case classes" should {
      "handle simple case class" in {
        JSON.readAndGet[CaseClass1](JObject(CaseClassSupport.ClassKey -> JString(classOf[CaseClass1].getName), "name" -> JString("John Doe"))) should equal(CaseClass1("John Doe"))
      }
      "handle simple case class without class info" in {
        JSON.readAs[CaseClass1](JObject("name" -> JString("Jane Doe"))) should equal(Some(CaseClass1("Jane Doe")))
      }
    }
    "parsing case classes" should {
      "handle simple case class" in {
        JSON.parseAndGet(CaseClass1("John Doe")) should equal(JObject(CaseClassSupport.ClassKey -> JString(classOf[CaseClass1].getName), "name" -> JString("John Doe")))
      }
      "handle simple case class without writing class info" in {
        JSON.dontWriteExtras {
          JSON.parseAndGet(CaseClass1("John Doe")) should equal(JObject("name" -> JString("John Doe")))
        }
      }
    }
  }
}

case class CaseClass1(name: String)