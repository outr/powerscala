package org.powerscala.ref

import enumeratum._

sealed trait ReferenceType extends EnumEntry {
  def apply[T <: AnyRef](value: T): Reference[T]
}

object ReferenceType extends Enum[ReferenceType] {
  case object Hard extends ReferenceType {
    def apply[T <: AnyRef](value: T) = HardReference(value)
  }
  case object Soft extends ReferenceType {
    def apply[T <: AnyRef](value: T) = SoftReference(value)
  }
  case object Weak extends ReferenceType {
    def apply[T <: AnyRef](value: T) = WeakReference(value)
  }

  val values = findValues.toVector
}