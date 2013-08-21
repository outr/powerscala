package org.powerscala.json

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class JSONSpec extends WordSpec with ShouldMatchers {
  JSONConverter.registerType("org.powerscala.json.TestJSON1.values", classOf[Int])
  JSONConverter.registerType("org.powerscala.json.TestJSON2.values", classOf[String])

  val t1 = TestJSON1("Test1", List(1, 2, 3))
  lazy val o1 = generate(t1)
  lazy val o1b = generate(t1, specifyClassName = false)
  val e1 = """{"name" : "Test1", "values" : [1, 2, 3], "class" : "org.powerscala.json.TestJSON1"}"""
  val e1b = """{"name" : "Test1", "values" : [1, 2, 3]}"""
  lazy val l1 = parse[Any](e1)(null)

  val t2 = TestJSON2("Test2", List("One", "Two", "Three"))
  lazy val o2 = generate(t2)
  val e2 = """{"name" : "Test2", "values" : ["One", "Two", "Three"], "class" : "org.powerscala.json.TestJSON2"}"""
  lazy val l2 = parse[Any](e2)(null)

  val t3 = TestJSON3("Test3", List(Test(1), Test(2), Test(3)))
  lazy val o3 = generate(t3)
  val e3 = """{"name" : "Test3", "values" : [{"value" : 1, "class" : "org.powerscala.json.Test"}, {"value" : 2, "class" : "org.powerscala.json.Test"}, {"value" : 3, "class" : "org.powerscala.json.Test"}], "class" : "org.powerscala.json.TestJSON3"}"""
  lazy val l3 = parse[Any](e3)(null)

  "JSON" should {
    "save JSON with List[Int] successfully" in {
      o1 should equal(e1)
    }
    "save JSON with List[Int] without class details" in {
      o1b should equal(e1b)
    }
    "load JSON with List[Int] successfully" in {
      l1.getClass should equal(classOf[TestJSON1])
      val t = l1.asInstanceOf[TestJSON1]
      t.name should equal("Test1")
      t.values.length should equal(3)
      t.values should equal(List(1, 2, 3))
    }
    "save JSON with List[String] successfully" in {
      o2 should equal(e2)
    }
    "load JSON with List[String] successfully" in {
      l2.getClass should equal(classOf[TestJSON2])
      val t = l2.asInstanceOf[TestJSON2]
      t.name should equal("Test2")
      t.values.length should equal(3)
      t.values should equal(List("One", "Two", "Three"))
    }
    "save JSON with List[Test] successfully" in {
      o3 should equal(e3)
    }
    "load JSON with List[Test] successfully" in {
      l3.getClass should equal(classOf[TestJSON3])
      val t = l3.asInstanceOf[TestJSON3]
      t.name should equal("Test3")
      t.values.length should equal(3)
      t.values should equal(List(Test(1), Test(2), Test(3)))
    }
  }
}

case class Test(value: Int)

case class TestJSON1(name: String, values: List[Int])

case class TestJSON2(name: String, values: List[String])

case class TestJSON3(name: String, values: List[Test])