package org.powerscala.datastore.query

import org.powerscala.enum.{EnumEntry, Enumerated}

class SortDirection private() extends EnumEntry

object SortDirection extends Enumerated[SortDirection] {
  val Ascending = new SortDirection
  val Descending = new SortDirection
}
