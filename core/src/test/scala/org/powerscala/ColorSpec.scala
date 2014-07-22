package org.powerscala

import org.scalatest.{Matchers, WordSpec}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ColorSpec extends WordSpec with Matchers {
  "Color" should {
    "properly parse 'red'" in {
      Color.get("red") should equal(Some(Color.Red))
    }
  }
}
