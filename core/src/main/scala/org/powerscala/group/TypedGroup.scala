package org.powerscala.group

sealed trait TypedGroup[T] {
  def toVector: Vector[T]
}

object TypedGroup {
  def apply[T](t: T): TypedGroup1[T] = TypedGroup1(t)
  def apply[T, T1 <: T, T2 <: T](t1: T1, t2: T2): TypedGroup2[T, T1, T2] = TypedGroup2(t1, t2)
  def apply[T, T1 <: T, T2 <: T, T3 <: T](t1: T1, t2: T2, t3: T3): TypedGroup3[T, T1, T2, T3] = TypedGroup3(t1, t2, t3)
  def apply[T, T1 <: T, T2 <: T, T3 <: T, T4 <: T](t1: T1, t2: T2, t3: T3, t4: T4): TypedGroup4[T, T1, T2, T3, T4] = {
    TypedGroup4(t1, t2, t3, t4)
  }
  def apply[T, T1 <: T, T2 <: T, T3 <: T, T4 <: T, T5 <: T](t1: T1, t2: T2, t3: T3, t4: T4, t5: T5): TypedGroup5[T, T1, T2, T3, T4, T5] = {
    TypedGroup5(t1, t2, t3, t4, t5)
  }
  def apply[T, T1 <: T, T2 <: T, T3 <: T, T4 <: T, T5 <: T, T6 <: T](t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6): TypedGroup6[T, T1, T2, T3, T4, T5, T6] = {
    TypedGroup6(t1, t2, t3, t4, t5, t6)
  }
}

case class TypedGroup1[T](t: T) extends TypedGroup[T] {
  def toVector: Vector[T] = Vector(t)
}

case class TypedGroup2[T, T1 <: T, T2 <: T](t1: T1, t2: T2) extends TypedGroup[T] {
  def toVector: Vector[T] = Vector(t1, t2)
}

case class TypedGroup3[T, T1 <: T, T2 <: T, T3 <: T](t1: T1, t2: T2, t3: T3) extends TypedGroup[T] {
  def toVector: Vector[T] = Vector(t1, t2, t3)
}

case class TypedGroup4[T, T1 <: T, T2 <: T, T3 <: T, T4 <: T](t1: T1, t2: T2, t3: T3, t4: T4) extends TypedGroup[T] {
  def toVector: Vector[T] = Vector(t1, t2, t3, t4)
}

case class TypedGroup5[T, T1 <: T, T2 <: T, T3 <: T, T4 <: T, T5 <: T](t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) extends TypedGroup[T] {
  def toVector: Vector[T] = Vector(t1, t2, t3, t4, t5)
}

case class TypedGroup6[T, T1 <: T, T2 <: T, T3 <: T, T4 <: T, T5 <: T, T6 <: T](t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) extends TypedGroup[T] {
  def toVector: Vector[T] = Vector(t1, t2, t3, t4, t5, t6)
}