package org.powerscala

import enumeratum._

/**
 * Precision is an enum defining numeric precisions and conversions.
 */
sealed abstract class Precision(val conversion: Double, f: () => Long) extends EnumEntry {
  def time = f()
}

object Precision extends Enum[Precision] {
  case object Milliseconds extends Precision(1000.0, () => System.currentTimeMillis)
  case object Nanoseconds extends Precision(1000000000.0, () => System.nanoTime)

  val values = findValues.toVector
}