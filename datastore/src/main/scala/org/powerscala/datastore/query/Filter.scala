package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

sealed trait Filter[T <: Identifiable]

case class FieldFilter[T <: Identifiable](field: Field[T, _], operator: Operator, value: Any) extends Filter[T]

sealed trait SubFilter[T <: Identifiable] extends Filter[T] {
  def filters: Seq[Filter[T]]
}

case class OrFilter[T <: Identifiable](filters: Seq[Filter[T]]) extends SubFilter[T]

case class AndFilter[T <: Identifiable](filters: Seq[Filter[T]]) extends SubFilter[T]
