package org.powerscala.ref

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 *
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed trait ReferenceType extends EnumEntry {
  def apply[T <: AnyRef](value: T): Reference[T]
}

object ReferenceType extends Enumerated[ReferenceType] {
  val Hard = new ReferenceType {
    def apply[T <: AnyRef](value: T) = HardReference(value)
  }
  val Soft = new ReferenceType {
    def apply[T <: AnyRef](value: T) = SoftReference(value)
  }
  val Weak = new ReferenceType {
    def apply[T <: AnyRef](value: T) = WeakReference(value)
  }
}