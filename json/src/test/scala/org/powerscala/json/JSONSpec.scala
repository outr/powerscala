package org.powerscala.json

import org.json4s._
import org.powerscala.Language
import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class JSONSpec extends WordSpec with Matchers {
  "JSON" when {
    "reading standard types" should {
      "handle Boolean" in {
        fromJSON(JBool(value = true)) should equal(true)
        fromJSON(JBool(value = false)) should equal(false)
      }
      "handle Int" in {
        fromJSON(JInt(5)) should equal(5)
      }
      "handle Double" in {
        fromJSON(JDouble(5.2)) should equal(5.2)
      }
      "handle Decimal" in {
        fromJSON(JDecimal(5.3)) should equal(BigDecimal(5.3))
      }
      "handle String" in {
        fromJSON(JString("Hello")) should equal("Hello")
      }
      "handle List" in {
        fromJSON(JArray(List(JString("Hello"), JInt(3), JDouble(1.2)))) should equal(List("Hello", 3, 1.2))
      }
      "handle Map" in {
        fromJSON(JObject("First" -> JInt(1), "Second" -> JDecimal(2.3), "Three" -> JString("Third"), "Four" -> JBool(value = false))) should equal(Map("First" -> 1, "Second" -> 2.3, "Three" -> "Third", "Four" -> false))
      }
    }
    "parsing standard types" should {
      "handle Boolean" in {
        toJSON(true) should equal(JBool(value = true))
        toJSON(false) should equal(JBool(value = false))
      }
      "handle Int" in {
        toJSON(5) should equal(JInt(5))
      }
      "handle Double" in {
        toJSON(5.2) should equal(JDouble(5.2))
      }
      "handle Decimal" in {
        toJSON(BigDecimal(5.3)) should equal(JDecimal(5.3))
      }
      "handle String" in {
        toJSON("Hello") should equal(JString("Hello"))
      }
      "handle List" in {
        toJSON(List(1, 2.2, "Hello", false)) should equal(JArray(List(JInt(1), JDouble(2.2), JString("Hello"), JBool(value = false))))
      }
      "handle Map" in {
        toJSON(Map("First" -> 1, "Second" -> BigDecimal(2.3), "Third" -> "Goodbye", "Fourth" -> true)) should equal(JObject("First" -> JInt(1), "Second" -> JDecimal(2.3), "Third" -> JString("Goodbye"), "Fourth" -> JBool(value = true)))
      }
    }
    "handling custom types" should {
      "convert Enum to JSON" in {
        toJSON(Language.English).compact should equal("""{"enumClass":"org.powerscala.Language","name":"English"}""")
      }
      "convert Enum from JSON" in {
        fromJSON("""{"enumClass":"org.powerscala.Language","name":"English"}""") should equal(Language.English)
      }
      "convert Option[Int] to JSON" in {
        toJSON(Option(5)).compact should equal("""{"option":5}""")
      }
      "convert Option[Int] from JSON" in {
        fromJSON("""{"option":5}""") should equal(Some(5))
      }
      "convert None to JSON" in {
        toJSON(None).compact should equal("""{"option":null}""")
      }
      "convert None from JSON" in {
        fromJSON("""{"option":null}""") should equal(None)
      }
    }
    "dealing with case classes" should {
      "convert CaseClass1 to JSON" in {
        toJSON(CaseClass1("First")).compact should equal("""{"class":"org.powerscala.json.CaseClass1","name":"First"}""")
      }
      "convert CaseClass1 from JSON" in {
        fromJSON("""{"class":"org.powerscala.json.CaseClass1","name":"First"}""") should equal(CaseClass1("First"))
      }
      "convert CaseClass2 to JSON" in {
        toJSON(CaseClass2("Second", CaseClass1("Inner"))).compact should equal("""{"class":"org.powerscala.json.CaseClass2","name":"Second","c1":{"class":"org.powerscala.json.CaseClass1","name":"Inner"}}""")
      }
      "convert CaseClass2 from JSON" in {
        fromJSON("""{"class":"org.powerscala.json.CaseClass2","name":"Second","c1":{"class":"org.powerscala.json.CaseClass1","name":"Inner"}}""") should equal(CaseClass2("Second", CaseClass1("Inner")))
      }
    }
    "dealing with typed case classes" should {
      "register type for CaseClass1" in {
        TypedSupport.register("one", classOf[CaseClass1])
      }
      "convert CaseClass1 to JSON" in {
        toJSON(CaseClass1("Third")).compact should equal("""{"type":"one","name":"Third"}""")
      }
      "convert CaseClass1 from JSON" in {
        fromJSON("""{"type":"one","name":"Third"}""") should equal(CaseClass1("Third"))
      }
      "register type for CaseClass2" in {
        TypedSupport.register("two", classOf[CaseClass2])
      }
      "convert CaseClass2 to JSON" in {
        toJSON(CaseClass2("Fourth", CaseClass1("Fifth"))).compact should equal("""{"type":"two","name":"Fourth","c1":{"type":"one","name":"Fifth"}}""")
      }
      "convert CaseClass2 from JSON" in {
        fromJSON("""{"type":"two","name":"Fourth","c1":{"type":"one","name":"Fifth"}}""") should equal(CaseClass2("Fourth", CaseClass1("Fifth")))
      }
      "register types for Events" in {
        TypedSupport.register("event", classOf[EventWrapper])
        TypedSupport.register("bool", classOf[BooleanEvent])
        TypedSupport.register("int", classOf[IntEvent])
      }
      "convert EventWrapper with BooleanEvent to JSON" in {
        toJSON(EventWrapper("e1", BooleanEvent("True", b = true))).compact should equal("""{"type":"event","name":"e1","event":{"type":"bool","name":"True","b":true}}""")
      }
      "convert EventWrapper with BooleanEvent from JSON" in {
        fromJSON("""{"type":"event","name":"e1","event":{"type":"bool","name":"True","b":true}}""") should equal(EventWrapper("e1", BooleanEvent("True", b = true)))
      }
      "convert EventWrapper with IntEvent to JSON" in {
        toJSON(EventWrapper("e2", IntEvent("Fifty", 50))).compact should equal("""{"type":"event","name":"e2","event":{"type":"int","name":"Fifty","value":50}}""")
      }
      "convert EventWrapper with IntEvent from JSON" in {
        fromJSON("""{"type":"event","name":"e2","event":{"type":"int","name":"Fifty","value":50}}""") should equal(EventWrapper("e2", IntEvent("Fifty", 50)))
      }
    }
    // TODO: support removal of type and/or class for registered types or all
    // TODO: support typed retrieval (fromJSON[Type]) to inject "class" into JObject
  }
}

case class CaseClass1(name: String)

case class CaseClass2(name: String, c1: CaseClass1)

case class EventWrapper(name: String, event: Event)

trait Event

case class BooleanEvent(name: String, b: Boolean) extends Event

case class IntEvent(name: String, value: Int) extends Event