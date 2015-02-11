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

  "Color" should {
    "properly stringify 'red' in CSS format" in {
      Color.get("red").map(_.toCSS) should equal(Some("#ff0000"))
      Color.get("red").map(_.subtract(alpha = 0.5f).toCSS) should equal(Some("rgba(255, 0, 0, 0.5)"))
    }
  }
}
