package org.powerscala.enum

import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class EnumEntry {
  /**
   * Enumerated for this EnumEntry.
   */
  lazy val parent = getClass.findCompanionOfType[Enumerated[EnumEntry]].getOrElse(throw new NullPointerException(s"Unable to find companion for $getClass"))
  /**
   * Name for this EnumEntry.
   */
  lazy val name = parent.enumName(this)
  /**
   * Ordinal for this EnumEntry.
   */
  lazy val ordinal = parent.enumOrdinal(this)
  /**
   * Generated label for this EnumEntry.
   */
  lazy val label = CaseValue.generateLabel(name)

  parent += this

  /**
   * Adds additional lookup validation to match on Enumerated.apply
   *
   * @param s the String to validate
   * @return defaults to false
   */
  def isMatch(s: String) = false

  override def toString = s"${parent.name}.$name"
}
