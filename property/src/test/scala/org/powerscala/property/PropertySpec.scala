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
      val sp = Property[String]("sp", null)(null, implicitly[Manifest[String]])
      val ip = Property[Int]("ip", 0)(null, implicitly[Manifest[Int]])
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
      val test = Property[String]("test", "Initial")(null, implicitly[Manifest[String]])
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
}

object PropertyTester extends PropertyParent with Listenable with NamedChild {
  val parent: PropertyParent = null

  object inner extends PropertyParent with Listenable with NamedChild {
    override def parent = PropertyTester

    val p = Property[Int]("p", 0)
  }
}