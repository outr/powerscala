package org.powerscala.reflect

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FromString[T] {
  def get(value: String): Option[T]
}

object FromString {
  def get(value: String, clazz: EnhancedClass) = clazz.instance match {
    case Some(i) => i match {
      case f: FromString[_] => f.get(value)
      case _ => None
    }
    case None => None
  }
}