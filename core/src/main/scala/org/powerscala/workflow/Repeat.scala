package org.powerscala.workflow

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Repeat private() extends EnumEntry

object Repeat extends Enumerated[Repeat] {
  val All = new Repeat()
  val First = new Repeat()
  val Last = new Repeat()
}