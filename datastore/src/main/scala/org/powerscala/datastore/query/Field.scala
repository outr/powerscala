package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

import java.util

case class Field[T <: Identifiable, F](name: String) {
  def <(value: F) = Filter(this, Operator.<, value)

  def <=(value: F) = Filter(this, Operator.<=, value)

  def >(value: F) = Filter(this, Operator.>, value)

  def >=(value: F) = Filter(this, Operator.>=, value)

  def equal(value: F) = Filter(this, Operator.equal, value)

  def nequal(value: F) = Filter(this, Operator.nequal, value)

  def ascending = Sort(this, SortDirection.Ascending)

  def descending = Sort(this, SortDirection.Descending)
}

object Field {
  def id[T <: Identifiable] = Field[T, util.UUID]("_id")
}