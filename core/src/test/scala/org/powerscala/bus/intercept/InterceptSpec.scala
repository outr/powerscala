package org.powerscala.bus.intercept

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.bus.Routing

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
      one {
        case s => Routing.Response(s.reverse)
      }
      two {
        case s => Routing.Response(s.toUpperCase)
      }
      three {
        case s => Routing.Stop
      }
      four {
        case s => Routing.Continue
      }
    }
    "send an intercept to 'one' and have it replaced" in {
      val responseOption = one.fire("Testing")
      responseOption.nonEmpty should equal(true)
      responseOption.get should equal("gnitseT")
    }
    "send an intercept to 'two' and have it replaced" in {
      val responseOption = two.fire("Testing")
      responseOption.nonEmpty should equal(true)
      responseOption.get should equal("TESTING")
    }
    "send an intercept to 'three' and have it rejected" in {
      val responseOption = three.fire("Testing")
      responseOption.isEmpty should equal(true)
    }
    "send an intercept to 'four' and have it continue" in {
      val responseOption = four.fire("Testing")
      responseOption.nonEmpty should equal(true)
      responseOption.get should equal("Testing")
    }
  }
}