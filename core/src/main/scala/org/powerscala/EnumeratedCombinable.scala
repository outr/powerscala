package org.powerscala

/**
 * EnumeratedCombinable extends upon Enumerated to allow combining of enums to offer similar
 * functionality to bit flags.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait EnumeratedCombinable[E <: EnumEntry[E]] extends Enumerated[E] {
  /**
   * Responsible for returning an enum that combines the supplied enums
   */
  def combine(enums: E*): E with Combined[E]
}

trait Combined[T] {
  def combined: List[T]

  override def equals(obj: Any) = !combined.find(e => e == obj).isEmpty

  override def toString = combined.mkString("[", ", ", "]")
}