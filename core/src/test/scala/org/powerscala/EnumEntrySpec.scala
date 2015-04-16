package org.powerscala

import org.scalatest.{Matchers, WordSpec}
import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 *
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnumEntrySpec extends WordSpec with Matchers {
  "TestEnum" should {
    "have three entries" in {
      TestEnum.values.length should be(3)
    }
    "have 'One' as the first entry" in {
      TestEnum.values(0).name should be("One")
    }
    "have 'Two' as the second entry" in {
      TestEnum.values(1).name should be("Two")
    }
    "have 'Three' as the third entry" in {
      TestEnum.values(2).name should be("Three")
    }
    "have 0 as the ordinal of One" in {
      TestEnum.One.ordinal should be(0)
    }
    "have 1 as the ordinal of Two" in {
      TestEnum.Two.ordinal should be(1)
    }
    "have 2 as the ordinal of Three" in {
      TestEnum.Three.ordinal should be(2)
    }
    "find 'One' by name" in {
      TestEnum("One") should be(TestEnum.One)
    }
    "find 'Two' by name" in {
      TestEnum("Two") should be(TestEnum.Two)
    }
    "find 'Three' by name" in {
      TestEnum("Three") should be(TestEnum.Three)
    }
    "not find equality between 'One' and 'Two'" in {
      TestEnum.One should not be TestEnum.Two
    }
    "not find equality between 'Two' and 'Three'" in {
      TestEnum.Two should not be TestEnum.Three
    }
    "not find equality between 'Three' and 'One'" in {
      TestEnum.Three should not be TestEnum.One
    }
  }
  "Currency" should {
    "iterate over very fast" in {
      Currency.values.toList      // Pre-access
      val time = System.nanoTime()
      val values = Currency.values.toList
      values.length should equal(Currency.values.length)
      val elapsed = System.nanoTime() - time
      elapsed should be < 20000000L
    }
  }
}

sealed trait TestEnum extends EnumEntry {
  lazy val ordinal = TestEnum.values.indexOf(this)
}

object TestEnum extends Enumerated[TestEnum] {
  case object One extends TestEnum
  case object Two extends TestEnum
  case object Three extends TestEnum

  val values = findValues.toVector
}