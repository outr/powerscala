package org.powerscala

import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Unique {
  private def r = ThreadLocalRandom.current()

  @tailrec
  final def apply(): String = new UUID(r.nextLong(), r.nextLong()).toString.replace("-", "") match {
    case s if !s.charAt(0).isDigit => s
    case s => apply()
  }
}