package org.powerscala.enum

import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class EnumEntry {
  /**
   * Enumerated for this EnumEntry.
   */
  val parent = getClass.findCompanionOfType[Enumerated[EnumEntry]].getOrElse(throw new NullPointerException(s"Unable to find companion for $getClass"))
  val (name, ordinal) = parent.define(this)
  /**
   * Generated label for this EnumEntry.
   */
  lazy val label = CaseValue.generateLabel(name)

  /**
   * Adds additional lookup validation to match on Enumerated.apply
   *
   * @param s the String to validate
   * @return defaults to false
   */
  def isMatch(s: String) = false

  override def toString = s"${parent.name}.$name"
}
