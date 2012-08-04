package org.powerscala

import naming.NamedChild


/**
 * EnumEntries should be instanced within an Enumerated companion object.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
abstract class EnumEntry[E <: EnumEntry[E]](implicit val parent: Enumerated[E]) extends NamedChild {
  /**
   * The index of this EnumEntry.
   */
  lazy val ordinal = parent.values.indexOf(this)

  override def toString = if (parent != null) {
    "%s.%s".format(parent.name, name)
  } else {
    name()
  }
}