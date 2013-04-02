package org

package object powerscala {
  import language.implicitConversions

  type Interceptor[T] = (T, T, T) => Option[T]

  implicit def double2Float(value: Double) = value.toFloat
}