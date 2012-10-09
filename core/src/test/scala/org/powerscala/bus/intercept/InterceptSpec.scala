package org.powerscala.bus.intercept

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class InterceptSpec extends WordSpec with ShouldMatchers {
  val one = Interceptable[String]("one")
  val two = Interceptable[String]("two")
  val three = Interceptable[String]("three")
  val four = Interceptable[String]("four")

  "Interceptable" should {
    "configure replace intercepts" in {
      one.intercept(new Intercept[String] {
        def intercept(t: String) = replace(t.reverse)
      })
      two.intercept(new Intercept[String] {
        def intercept(t: String) = replace(t.toUpperCase)
      })
      three.intercept(new Intercept[String] {
        def intercept(t: String) = reject()
      })
      four.intercept(new Intercept[String] {
        def intercept(t: String) = continue()
      })
    }
    "send an intercept to 'one' and have it replaced" in {
      val responseOption = one("Testing")
      responseOption.nonEmpty should equal(true)
      responseOption.get should equal("gnitseT")
    }
    "send an intercept to 'two' and have it replaced" in {
      val responseOption = two("Testing")
      responseOption.nonEmpty should equal(true)
      responseOption.get should equal("TESTING")
    }
    "send an intercept to 'three' and have it rejected" in {
      val responseOption = three("Testing")
      responseOption.isEmpty should equal(true)
    }
    "send an intercept to 'four' and have it continue" in {
      val responseOption = four("Testing")
      responseOption.nonEmpty should equal(true)
      responseOption.get should equal("Testing")
    }
  }
}