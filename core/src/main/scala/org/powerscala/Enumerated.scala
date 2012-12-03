package org.powerscala

import naming.{NamingFilter, NamingParent}

/**
 * Enumerated represents the companion object for EnumEntry instances.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Enumerated[E <: EnumEntry[E]] extends NamingParent {
  implicit val enumerated = this

  /**
   * The name of this Enumerated.
   */
  lazy val name = getClass.getSimpleName.replaceAll("\\$", "")

  def apply(name: String): E = apply(name, caseSensitive = false)

  /**
   * Retrieve the EnumEntry by name.
   *
   * @param name the name of the EnumEntry as defined by the field.
   * @param caseSensitive defines whether the lookup should be case-sensitive. Defaults to false.
   * @return EnumEntry or null if not found
   */
  def apply(name: String, caseSensitive: Boolean) = {
    values.find(e => (caseSensitive && e.name() == name) || (!caseSensitive && name.equalsIgnoreCase(e.name()))).getOrElse(null.asInstanceOf[E])
  }

  /**
   * Retrieve the EnumEntry by index.
   *
   * @param index of the EnumEntry.
   * @return EnumEntry or IndexOutOfBoundsException
   */
  def apply(index: Int) = values(index)

  /**
   * All EnumEntries for this Enumerated instance.
   */
  lazy val values = new NamingFilter[E](this)
}