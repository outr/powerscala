package org.powerscala.bind

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.event.ChangeEvent

/**
 * BindingSpec tests Binding and Bindable classes.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class BindingSpec extends WordSpec with ShouldMatchers {
  "Bindable" when {
    "single binding" should {
      "set the initial values" in {
        BindTest1.value = 0
        BindTest2.value = 0
        BindTest3.value = "0"
      }
      "have no listeners on either BindTest1 or BindTest2" in {
        BindTest1.listeners.values.isEmpty should equal(true)
        BindTest2.listeners.values.isEmpty should equal(true)
      }
      "bind BindTest1 to BindTest2 successfully" in {
        BindTest1 bind BindTest2
      }
      "have one listener on BindTest2" in {
        BindTest1.listeners.values.isEmpty should equal(true)
        BindTest2.listeners.values.isEmpty should equal(false)
      }
      "have the correct initial values" in {
        BindTest1.value should equal(0)
        BindTest2.value should equal(0)
      }
      "update the value of BindTest2" in {
        BindTest2.value = 1
      }
      "BindTest2 should have the new value" in {
        BindTest2.value should equal(1)
      }
      "BindTest1 should reflect the new value" in {
        BindTest1.value should equal(1)
      }
      "BindTest1 should remove the binding" in {
        BindTest1 unbind BindTest2
      }
      "have no listeners on either test" in {
        BindTest1.listeners.values.isEmpty should equal(true)
        BindTest2.listeners.values.isEmpty should equal(true)
      }
    }
    "conversion binding" should {
      "set the initial values" in {
        BindTest1.value = 0
        BindTest2.value = 0
        BindTest3.value = "0"
      }
      "have no listeners on either BindTest1 or BindTest3" in {
        BindTest1.listeners.values.isEmpty should equal(true)
        BindTest3.listeners.values.isEmpty should equal(true)
      }
      "bind BindTest1 to BindTest3 successfully" in {
        implicit val s2i = (s: String) => s.toInt
        BindTest1.bindTo[String](BindTest3)
      }
      "have one listener on BindTest3" in {
        BindTest1.listeners.values.isEmpty should equal(true)
        BindTest3.listeners.values.isEmpty should equal(false)
      }
      "have the correct initial values" in {
        BindTest1.value should equal(0)
        BindTest3.value should equal("0")
      }
      "update the value of BindTest3" in {
        BindTest3.value = "1"
      }
      "BindTest3 should have the new value" in {
        BindTest3.value should equal("1")
      }
      "BindTest1 should reflect the new value" in {
        BindTest1.value should equal(1)
      }
      "BindTest1 should remove the binding" in {
        BindTest1.unbind(BindTest3)
      }
      "have no listeners on either test" in {
        BindTest1.listeners.values.isEmpty should equal(true)
        BindTest3.listeners.values.isEmpty should equal(true)
      }
    }
  }
}

class BindTest[T] extends Bindable[T] {
  private var _value: T = _

  def value = _value

  def value_=(_value: T) = {
    val evt = ChangeEvent(this._value, _value)
    this._value = _value
    fire(evt)
  }

  def apply(value: T) = this.value = value
}

object BindTest1 extends BindTest[Int]

object BindTest2 extends BindTest[Int]

object BindTest3 extends BindTest[String]