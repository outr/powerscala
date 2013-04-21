package org.powerscala.datastore.query


import java.util
import org.powerscala.datastore.{Identifiable, LazyList, Lazy}
import util.regex.Pattern

trait Field[T, F] {
  def name: String
  def manifest: Manifest[T]
}

object Field {
  def basic[T, F](name: String)(implicit manifest: Manifest[T]) = new BaseField[T, F](name)

  def id[T](implicit manifest: Manifest[T]) = new BaseField[T, util.UUID]("_id")

  def uuid[T](name: String)(implicit manifest: Manifest[T]) = new BaseField[T, util.UUID](name)

  def boolean[T](name: String)(implicit manifest: Manifest[T]) = new BaseField[T, Boolean](name)

  def byte[T](name: String)(implicit manifest: Manifest[T]) = new NumericField[T, Byte](name)

  def int[T](name: String)(implicit manifest: Manifest[T]) = new NumericField[T, Int](name)

  def float[T](name: String)(implicit manifest: Manifest[T]) = new NumericField[T, Float](name)

  def long[T](name: String)(implicit manifest: Manifest[T]) = new NumericField[T, Long](name)

  def double[T](name: String)(implicit manifest: Manifest[T]) = new NumericField[T, Double](name)

  def string[T](name: String)(implicit manifest: Manifest[T]) = new StringField[T](name)

  def embedded[T, F](name: String)(implicit manifest: Manifest[T]) = new EmbeddedField[T, F](name)

  def lzy[T, F <: Identifiable](name: String)(implicit manifest: Manifest[T]) = new EmbeddedField[T, Lazy[F]](name)

  def lazyList[T, F <: Identifiable](name: String)(implicit manifest: Manifest[T]) = new EmbeddedField[T, LazyList[F]](name)
}

class BaseField[T, F](val name: String)(implicit val manifest: Manifest[T]) extends Field[T, F] {
  def equal(value: F) = FieldFilter(this, Operator.equal, value)

  def nequal(value: F) = FieldFilter(this, Operator.nequal, value)

  lazy val exists = FieldFilter(this, Operator.exists, true)
  lazy val nexists = FieldFilter(this, Operator.exists, false)

  def in(values: F*) = FieldFilter(this, Operator.in, values)

  def ascending = Sort(this, SortDirection.Ascending)

  def descending = Sort(this, SortDirection.Descending)
}

class NumericField[T, F](name: String)(implicit manifest: Manifest[T]) extends BaseField[T, F](name)(manifest) {
  def <(value: F) = FieldFilter(this, Operator.<, value)

  def <=(value: F) = FieldFilter(this, Operator.<=, value)

  def >(value: F) = FieldFilter(this, Operator.>, value)

  def >=(value: F) = FieldFilter(this, Operator.>=, value)
}

class StringField[T](name: String)(implicit manifest: Manifest[T]) extends BaseField[T, String](name)(manifest) {
  def regex(value: String, flags: RegexFlag*): FieldFilter[T] = {
    val bitFlags = flags.foldLeft(0)((value, flag) => value | flag.flag)
    val pattern = Pattern.compile(value.toString, bitFlags)
    regex(pattern)
  }

  def regex(pattern: Pattern) = FieldFilter(this, Operator.regex, pattern)
}

class EmbeddedField[T, F](name: String)(implicit manifest: Manifest[T]) extends BaseField[T, F](name)(manifest) {
  def apply[S](filter: Filter[F]) = FieldFilter(this, Operator.subfilter, filter)

  def sub[S](filter: Filter[F]) = apply[S](filter)
}