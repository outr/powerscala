package org.powerscala.enum

import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class EnumEntry {
  /**
   * Enumerated for this EnumEntry.
   */
  lazy val parent = companion(getClass)
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

  private def companion(c: Class[_]): Enumerated[EnumEntry] = c.instance match {
    case Some(companion) => companion.asInstanceOf[Enumerated[EnumEntry]]
    case None if (c.getSuperclass == null) => throw new NullPointerException(s"No companion object found for $getClass")
    case None => companion(c.getSuperclass)
  }

  override def toString = s"${parent.name}.$name"
}
