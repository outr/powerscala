package org.powerscala.bus.intercept

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.bus.{Bus, Routing}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class InterceptSpec extends WordSpec with ShouldMatchers {
  val bus = new Bus()
  val one = Interceptable[String]("one", bus)
  val two = Interceptable[String]("two", bus)
  val three = Interceptable[String]("three", bus)
  val four = Interceptable[String]("four", bus)

  "Interceptable" should {
    "configure replace intercepts" in {
      one.intercept {
        case s => Routing.Response(s.reverse)
      }
      two.intercept {
        case s => Routing.Response(s.toUpperCase)
      }
      three.intercept {
        case s => Routing.Stop
      }
      four.intercept {
        case s => Routing.Continue
      }
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