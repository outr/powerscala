package org.powerscala.property

import org.powerscala.reflect._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class StaticProperty[T](parent: MutableProperty[_], _name: String)(implicit manifest: Manifest[T]) extends MutableProperty[T] {
  private def p = parent().asInstanceOf[AnyRef]
  private lazy val caseValue = p.getClass.caseValue(_name).getOrElse(throw new NullPointerException("Case Value '%s' not found in %s".format(_name, p.getClass)))
  private def currentValue = loadValue()

  val name = () => _name

  def apply(value: T) = {
    val updated = caseValue.copy[AnyRef](p, value)
    parent.asInstanceOf[MutableProperty[Any]] := updated
  }

  def apply() = currentValue

  private def loadValue(): T = {
    p match {
      case null => manifest.runtimeClass.defaultForType[T]
      case _ => caseValue.apply[T](p)
    }
  }
}