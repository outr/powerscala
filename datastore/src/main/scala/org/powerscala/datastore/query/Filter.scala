package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

sealed trait Filter[T <: Identifiable] {
  def operator: Operator
}

case class FieldFilter[T <: Identifiable](field: Field[T, _], operator: Operator, value: Any) extends Filter[T]

case class SubFilter[T <: Identifiable](operator: Operator, filters: Seq[Filter[T]]) extends Filter[T]