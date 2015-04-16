package org.powerscala.enum

import enumeratum.Enum

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Enumerated[E <: EnumEntry] extends Enum[E] {
  def values: Vector[E]

  def apply(name: String) = get(name).getOrElse(throw new RuntimeException(s"Unable to find '$name' by name."))

  def get(name: String, caseSensitive: Boolean = false) = if (caseSensitive) {
    withNameOption(name)
  } else {
    withNameInsensitiveOption(name)
  }

  def unapply(s: String) = get(s)
}