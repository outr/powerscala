package org.powerscala.reflect

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.WordSpec

/**
 *
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnhancedSpec extends WordSpec with ShouldMatchers {
  var ec: EnhancedClass = _
  var method: EnhancedMethod = _

  val tc = new TestClass

  "EnhancedClass" when {
    "requested on String" should {
      "not return null" in {
        ec = classOf[String]
        ec should not be (null)
      }
      "have the proper name" in {
        ec.name should be("String")
      }
      "not have a companion class" in {
        ec.companion should be(None)
      }
    }
    "request on TestClass" should {
      "not return null" in {
        ec = classOf[TestClass]
        ec should not be (null)
      }
      "have the proper name" in {
        ec.name should be("org.powerscala.reflect.TestClass")
      }
      "have the proper number of methods" in {
        ec.methods.length should be(12)
      }
      "have a companion class" in {
        ec.companion should not be (None)
      }
      "have the testMethod" in {
        method = ec.method("testMethod", classOf[String], classOf[Int]).getOrElse(null)
        method should not be (null)
      }
      "invoke testMethod" in {
        method.invoke[String](tc, "Test", 2) should be("Test 2")
      }
      "have two arguments: s and i" in {
        method.args.length should be(2)
        method.args(0).name should be("s")
        method.args(1).name should be("i")
      }
      "have one default argument" in {
        method.args(0).hasDefault should be(false)
        method.args(1).hasDefault should be(true)
      }
      "have 5 for the default argument" in {
        method.args(1).default(tc) should be(Some(5))
      }
      "have String for the returnType" in {
        method.returnType.`type` should be(classOf[String])
      }
      "have a well defined toString on EnhancedMethod" in {
        method.toString should be("org.powerscala.reflect.TestClass.testMethod(s: String, i: Int): String")
      }
    }
    "working with a case class" should {
      val ec = EnhancedClass(classOf[TestCaseClass])
      val cc1 = TestCaseClass("My", "Test", 1)
      "have three CaseValues" in {
        ec.caseValues.length should equal(3)
      }
      "have the values in the correct order" in {
        ec.caseValues(0).name should equal("firstName")
        ec.caseValues(1).name should equal("lastName")
        ec.caseValues(2).name should equal("age")
      }
      "comprehend mutability properly" in {
        ec.caseValue("firstName").get.isMutable should equal(false)
        ec.caseValue("lastName").get.isMutable should equal(false)
        ec.caseValue("age").get.isMutable should equal(true)
      }
      "retrieve values properly" in {
        ec.caseValue("firstName").get.apply[String](cc1) should equal("My")
        ec.caseValue("lastName").get.apply[String](cc1) should equal("Test")
        ec.caseValue("age").get.apply[Int](cc1) should equal(1)
      }
      "modify a value properly" in {
        val cv = ec.caseValue("age").get
        cv(cc1) = 2
        cc1.age should equal(2)
      }
      "copy properly" in {
        val cc2 = ec.copy[TestCaseClass](cc1, Map("firstName" -> "Another"))
        cc2.firstName should equal("Another")
        cc2.lastName should equal("Test")
        cc2.age should equal(2)
      }
      "copy properly with no args" in {
        val clazz: EnhancedClass = classOf[TestCaseClass3]
        val original = TestCaseClass3()
        val copied = clazz.copy(original, Map.empty)
        assume(original == copied, "The two copies should be equal")
      }
      "instantiate case class" in {
        val cc3 = ec.create[TestCaseClass](Map("firstName" -> "One", "lastName" -> "Two", "age" -> 3))
        cc3.firstName should equal("One")
        cc3.lastName should equal("Two")
        cc3.age should equal(3)
      }
      "instantiate a case class with a default value" in {
        val cc4 = classOf[TestCaseClass2].create[TestCaseClass2](Map("name" -> "Test"))
        cc4.name should equal("Test")
        cc4.age should equal(5)
      }
      "detect transient CaseValues" in {
        val clazz: EnhancedClass = classOf[TestCaseClass4]
        val nameValue = clazz.caseValue("name").get
        val ageValue = clazz.caseValue("age").get
        nameValue.isTransient should equal(false)
        ageValue.isTransient should equal(true)
      }
    }
  }
}

class TestClass {
  def testMethod(s: String, i: Int = 5) = s + " " + i
}

object TestClass {
  def testCompanion() = "companion class: " + getClass.getName
}

case class TestCaseClass(firstName: String, lastName: String, var age: Int) {
  lazy val name = "%s %s".format(firstName, lastName)
}

case class TestCaseClass2(name: String, age: Int = 5)

case class TestCaseClass3()

case class TestCaseClass4(name: String, @transient age: Int)