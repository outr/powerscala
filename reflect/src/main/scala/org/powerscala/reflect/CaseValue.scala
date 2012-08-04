package org.powerscala.reflect

import java.lang.reflect.Modifier

/**
 * CaseValue represents a value on a case class.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class CaseValue(name: String, valueType: EnhancedClass, clazz: EnhancedClass) {
  lazy val getter = clazz.method(name)
  lazy val setter = clazz.methods.find(m => m.name == "%s_$eq".format(name))

  private lazy val field = clazz.javaClass.getDeclaredField(name)

  def isMutable = setter != None

  def isTransient = Modifier.isTransient(field.getModifiers)

  def apply[T](instance: AnyRef) = getter.getOrElse(throw new RuntimeException("Unable to getter for %s.%s".format(clazz, name))).invoke[T](instance)

  def update(instance: AnyRef, value: Any) = setter.get.invoke[Any](instance, value)

  /**
   * Copies <code>instance</code> setting the new value supplied for this CaseValue
   */
  def copy[T](instance: T, value: Any) = clazz.copy[T](instance, Map(name -> value))
}