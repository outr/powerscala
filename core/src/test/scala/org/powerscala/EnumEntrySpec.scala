package org.powerscala

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers

/**
 *
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnumEntrySpec extends WordSpec with ShouldMatchers {
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
}

sealed class TestEnum extends EnumEntry[TestEnum]

object TestEnum extends Enumerated[TestEnum] {
  val One = new TestEnum
  val Two = new TestEnum
  val Three = new TestEnum
}