package org.powerscala.enum

import enumeratum.Enum

import scala.util.Random

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Enumerated[E <: EnumEntry] extends Enum[E] {
  def values: Vector[E]

  def apply(index: Int) = values(index)

  def apply(name: String) = get(name).getOrElse(throw new RuntimeException(s"Unable to find '$name' by name."))

  def get(name: String, caseSensitive: Boolean = false) = {
    val option = if (caseSensitive) {
      withNameOption(name)
    } else {
      withNameInsensitiveOption(name)
    }
    if (option.nonEmpty) {
      option
    } else {
      values.find(e => e.isMatch(name))
    }
  }

  lazy val length = values.length

  def unapply(s: String) = get(s)

  def random = apply(Enumerated.r.nextInt(length))
}

object Enumerated {
  private lazy val r = new Random()
}