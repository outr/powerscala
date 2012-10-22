package org.powerscala.reflect

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnhancedValueSpec extends WordSpec with ShouldMatchers {
  val one = One("Test One", 1)
  val two = Two("Test Two", one)
  val three = Three("Test Three", two)

  val oneClass: EnhancedClass = classOf[One]
  val twoClass: EnhancedClass = classOf[Two]
  val threeClass: EnhancedClass = classOf[Three]

  "Enhanced value" when {
    "retrieving values" should {
      "retrieve a single depth String" in {
        oneClass.value[String](one, "name") should equal("Test One")
      }
      "retrieve a single depth Int" in {
        oneClass.value[Int](one, "value") should equal(1)
      }
      "retrieve a single depth One" in {
        twoClass.value[One](two, "one") should equal(one)
      }
      "retrieve a double depth String" in {
        twoClass.value[String](two, "one.name") should equal("Test One")
      }
      "retrieve a triple depth Int" in {
        threeClass.value[Int](three, "two.one.value") should equal(1)
      }
    }
    "setting values" should {
      "set a single depth String" in {
        val result = oneClass.modify[One](one, "name", "Modified One")
        result.name should equal("Modified One")
        result.value should equal(1)
      }
      "set a single depth Int" in {
        val result = oneClass.modify[One](one, "value", 2)
        result.name should equal("Test One")
        result.value should equal(2)
      }
      "set a single depth One" in {
        val result = twoClass.modify[Two](two, "one", One("Updated", 2))
        result.name should equal("Test Two")
        result.one.name should equal("Updated")
        result.one.value should equal(2)
      }
      "set a double depth String" in {
        val result = twoClass.modify[Two](two, "one.name", "Modified Two")
        result.name should equal("Test Two")
        result.one.name should equal("Modified Two")
        result.one.value should equal(1)
      }
      "set a triple depth Int" in {
        val result = threeClass.modify[Three](three, "two.one.value", 3)
        result.name should equal("Test Three")
        result.two.name should equal("Test Two")
        result.two.one.name should equal("Test One")
        result.two.one.value should equal(3)
      }
    }
  }
}

case class One(name: String, value: Int)

case class Two(name: String, one: One)

case class Three(name: String, two: Two)