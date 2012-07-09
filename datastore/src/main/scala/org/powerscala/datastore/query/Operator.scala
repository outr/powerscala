package org.powerscala.datastore.query

import org.powerscala.{EnumEntry, Enumerated}

sealed class Operator extends EnumEntry[Operator]

object Operator extends Enumerated[Operator] {
  val < = new Operator
  val <= = new Operator
  val > = new Operator
  val >= = new Operator
  val regex = new Operator
  val subfilter = new Operator
  val equal = new Operator
  val nequal = new Operator
}
