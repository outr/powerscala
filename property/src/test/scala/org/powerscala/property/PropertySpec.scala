package org.powerscala.property

import org.scalatest.{Matchers, WordSpec}
import org.powerscala.event.Listenable
import org.powerscala.hierarchy.event.Descendants
import org.powerscala.hierarchy.ChildLike
import org.powerscala.property.event.processor.PropertyChangeProcessor
import org.powerscala.Priority
import org.powerscala.property.event.PropertyChangeEvent
import scala.collection.mutable.ListBuffer
import org.powerscala.transaction
import org.powerscala.concurrent.Executor

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
  "Property Transactions" when {
    implicit val parent: Listenable = null
    val p1 = new Property[Int]() with TransactionProperty[Int]
    val p2 = new Property[String]() with TransactionProperty[String]
    var p1Changes = ListBuffer.empty[PropertyChangeEvent[Int]]
    var p2Changes = ListBuffer.empty[PropertyChangeEvent[String]]
    "configuring listeners" should {
      "add a listener to p1" in {
        p1.change.on(p1Changes += _)
      }
      "add a listener to p2" in {
        p2.change.on(p2Changes += _)
      }
    }
    "working with transactions" should {
      "set two values to p1 and commit" in {
        transaction {
          p1Changes should be(Nil)
          p1() should be(0)
          Executor.invokeFuture(p1()).get() should be(0)
          p1 := 1
          p1Changes should be(Nil)
          p1() should be(1)
          Executor.invokeFuture(p1()).get() should be(0)
          p1 := 2
          p1Changes should be(Nil)
          p1() should be(2)
          Executor.invokeFuture(p1()).get() should be(0)
        }
        p1() should be(2)
        Executor.invokeFuture(p1()).get() should be(2)
        p1Changes.length should be(1)
        p1Changes.head.oldValue should be(0)
        p1Changes.head.newValue should be(2)
      }
      "set two values to p1 and rollback" in {
        p1Changes.clear()
        transaction {
          p1Changes should be(Nil)
          p1() should be(2)
          Executor.invokeFuture(p1()).get() should be(2)
          p1 := 3
          p1Changes should be(Nil)
          p1() should be(3)
          Executor.invokeFuture(p1()).get() should be(2)
          p1 := 4
          p1Changes should be(Nil)
          p1() should be(4)
          Executor.invokeFuture(p1()).get() should be(2)
          transaction.rollback()
          p1Changes should be(Nil)
          p1() should be(2)
          Executor.invokeFuture(p1()).get() should be(2)
        }
        p1() should be(2)
        Executor.invokeFuture(p1()).get() should be(2)
        p1Changes.length should be(0)
      }
      "set values to p1 and p2 and commit" in {
        transaction {
          p1Changes should be(Nil)
          p2Changes should be(Nil)
          p1() should be(2)
          Executor.invokeFuture(p1()).get() should be(2)
          Executor.invokeFuture(p2()).get() should be(null)
          p1 := 5
          p2 := "One"
          p1Changes should be(Nil)
          p2Changes should be(Nil)
          p1() should be(5)
          p2() should be("One")
          Executor.invokeFuture(p1()).get() should be(2)
          Executor.invokeFuture(p2()).get() should be(null)
          transaction.commit()
          p1Changes.length should be(1)
          p2Changes.length should be(1)
          p1() should be(5)
          p2() should be("One")
          Executor.invokeFuture(p1()).get() should be(5)
          Executor.invokeFuture(p2()).get() should be("One")
          transaction.rollback()
        }
        p1Changes.length should be(1)
        p2Changes.length should be(1)
        p1() should be(5)
        p2() should be("One")
        Executor.invokeFuture(p1()).get() should be(5)
        Executor.invokeFuture(p2()).get() should be("One")
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