package org.powerscala.datastore.query

import language.existentials

sealed trait Filter[T] {
  def operator: Operator
}

case class FieldFilter[T](field: Field[T, _], operator: Operator, value: Any) extends Filter[T]

case class SubFilter[T](operator: Operator, filters: Seq[Filter[T]]) extends Filter[T]