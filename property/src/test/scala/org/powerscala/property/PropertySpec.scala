package org.powerscala.property

import org.scalatest.{Matchers, WordSpec}
import org.powerscala.event.Listenable
import org.powerscala.hierarchy.event.Descendants
import org.powerscala.hierarchy.ChildLike
import org.powerscala.property.event.processor.PropertyChangeProcessor
import org.powerscala.Priority

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class PropertySpec extends WordSpec with Matchers {
  "Property values" when {
    "created without a default value" should {
      val sp = Property[String]()
      val ip = Property[Int]()
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
    PropertyTester.inner.p.change.on {
      case event => direct += 1
    }
    PropertyTester.inner.change.listen(Priority.Normal, Descendants) {
      case event => inner += 1
    }
    PropertyTester.change.listen(Priority.Normal, Descendants) {
      case event => outter += 1
    }
    "hierarchy is validated" should {
      "have the correct parent for the property" in {
        PropertyTester.inner.p.parent should equal(PropertyTester.inner)
      }
      "have the correct parent for inner" in {
        ChildLike.parentOf(PropertyTester.inner) should equal(PropertyTester)
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
      val test = Property[String]()
      test := "Default"
      "add a cancelling and modifying listener" in {
        test.changing.on {
          case evt => if (evt == "Bad") {
            None
          } else if (evt == "New") {
            Some("Newer")
          } else {
            Some(evt)
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

object PropertyTester extends Listenable {
  val change = new PropertyChangeProcessor[Int]()
  object inner extends Listenable with ChildLike[Any] {
    protected def hierarchicalParent = PropertyTester

    val change = new PropertyChangeProcessor[Int]()
    val p = Property[Int]()
  }
}

case class StaticTest(one: String, two: Int, three: List[String])