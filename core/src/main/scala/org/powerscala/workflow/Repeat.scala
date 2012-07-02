package org.powerscala.workflow

import org.powerscala.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed class Repeat extends EnumEntry[Repeat]

object Repeat extends Enumerated[Repeat] {
  val All = new Repeat()
  val First = new Repeat()
  val Last = new Repeat()
}