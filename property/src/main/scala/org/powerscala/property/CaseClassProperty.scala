package org.powerscala.property

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait CaseClassProperty[T] {
  this: MutableProperty[T] =>

  def field[V](name: String)(implicit manifest: Manifest[V]) = new StaticProperty[V](this, name)(manifest)
}