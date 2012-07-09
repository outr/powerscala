package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

import java.util

trait Field[T <: Identifiable, F] {
  def name: String
}

object Field {
  def basic[T <: Identifiable, F](name: String) = new BaseField[T, F](name)

  def id[T <: Identifiable] = new BaseField[T, util.UUID]("_id")

  def boolean[T <: Identifiable](name: String) = new BaseField[T, Boolean](name)

  def byte[T <: Identifiable](name: String) = new NumericField[T, Byte](name)

  def int[T <: Identifiable](name: String) = new NumericField[T, Int](name)

  def float[T <: Identifiable](name: String) = new NumericField[T, Float](name)

  def long[T <: Identifiable](name: String) = new NumericField[T, Long](name)

  def double[T <: Identifiable](name: String) = new NumericField[T, Double](name)

  def string[T <: Identifiable](name: String) = new StringField[T](name)

  def embedded[T <: Identifiable, F <: Identifiable](name: String) = new EmbeddedField[T, F](name)
}

class BaseField[T <: Identifiable, F](val name: String) extends Field[T, F] {
  def equal(value: F) = Filter(this, Operator.equal, value)

  def nequal(value: F) = Filter(this, Operator.nequal, value)

  def ascending = Sort(this, SortDirection.Ascending)

  def descending = Sort(this, SortDirection.Descending)
}

class NumericField[T <: Identifiable, F](name: String) extends BaseField[T, F](name) {
  def <(value: F) = Filter(this, Operator.<, value)

  def <=(value: F) = Filter(this, Operator.<=, value)

  def >(value: F) = Filter(this, Operator.>, value)

  def >=(value: F) = Filter(this, Operator.>=, value)
}

class StringField[T <: Identifiable](name: String) extends BaseField[T, String](name) {
  def regex(value: String) = Filter(this, Operator.regex, value)
}

class EmbeddedField[T <: Identifiable, F <: Identifiable](name: String) extends BaseField[T, F](name) {
  def apply[S](filter: Filter[F]) = Filter(this, Operator.subfilter, filter)

  def sub[S](filter: Filter[F]) = apply[S](filter)
}