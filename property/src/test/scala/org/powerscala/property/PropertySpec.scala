package org.powerscala.property

import event.PropertyChangingEvent
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.event.{Listenable, ChangeEvent}
import org.powerscala.naming.NamedChild
import org.powerscala.bus.Routing

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class PropertySpec extends WordSpec with ShouldMatchers {
  "Property values" when {
    "created without a default value" should {
      val sp = Property[String].build()
      val ip = Property[Int].build()
      "have the correct default values" in {
        sp() should equal(null)
        ip() should equal(0)
      }
    }
  }
  "Property listeners" when {
    var direct = 0
    var inner = 0
    var outter = 0
    PropertyTester.inner.p.listeners.synchronous {
      case event: ChangeEvent => direct += 1
    }
    PropertyTester.inner.listeners.filter.descendant().synchronous {
      case event: ChangeEvent => inner += 1
    }
    PropertyTester.listeners.filter.descendant().synchronous {
      case event: ChangeEvent => outter += 1
    }
    "hierarchy is validated" should {
      "have the correct parent for the property" in {
        PropertyTester.inner.p.parent should equal(PropertyTester.inner)
      }
      "have the correct parent for inner" in {
        PropertyTester.inner.parent should equal(PropertyTester)
      }
      "have no parent for outter" in {
        PropertyTester.parent should equal(null)
      }
    }
    "changed" should {
      "update the value of the property" in {
        PropertyTester.inner.p := 5
      }
      "have updated the direct listener" in {
        direct should equal(1)
      }
      "have updated the inner listener" in {
        inner should equal(1)
      }
      "have updated the outter listener" in {
        outter should equal(1)
      }
    }
    "utilizing PropertyChangingEvents to intercept" should {
      val test = Property[String].default("Initial").build()
      "add a cancelling and modifying listener" in {
        test.listeners.synchronous {
          case evt: PropertyChangingEvent => if (evt.newValue == "Bad") {
            Routing.Stop
          } else if (evt.newValue == "New") {
            Routing.Response("Newer")
          }
        }
      }
      "change the value to something that won't get cancelled" in {
        test := "Good"
        test() should equal("Good")
      }
      "change the value to something that will get cancelled" in {
        test := "Bad"
        test() should equal("Good")
      }
      "change the value to something that will get modified" in {
        test := "New"
        test() should equal("Newer")
      }
    }
  }
  "StaticProperty" when {
    val p = new StandardProperty[StaticTest] {
      val one = field[String]("one")
      val two = field[Int]("two")
      val three = field[List[String]]("three")
    }

    "created" should {
      val t = StaticTest("1", 2, List("Uno", "Dos", "Tres"))
      p := t
      "represent the proper values" in {
        p.one() should equal("1")
        p.two() should equal(2)
        p.three() should equal(List("Uno", "Dos", "Tres"))
      }
      "change 'one' in property" in {
        p.one := "One"
        p.one() should equal("One")
        p.two() should equal(2)
        p.three() should equal(List("Uno", "Dos", "Tres"))
      }
      "change 'two' in property" in {
        p.two := 200
        p.one() should equal("One")
        p.two() should equal(200)
        p.three() should equal(List("Uno", "Dos", "Tres"))
      }
      "change 'three' in property" in {
        p.three := List("One", "Two", "Three")
        p.one() should equal("One")
        p.two() should equal(200)
        p.three() should equal(List("One", "Two", "Three"))
      }
    }
  }
}

object PropertyTester extends PropertyParent with Listenable with NamedChild {
  val parent: PropertyParent = null

  object inner extends PropertyParent with Listenable with NamedChild {
    override def parent = PropertyTester

    val p = Property[Int].default(0).build()
  }
}

case class StaticTest(one: String, two: Int, three: List[String])