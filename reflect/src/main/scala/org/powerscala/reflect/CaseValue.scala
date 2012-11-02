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

  def apply[T](instance: AnyRef) = getter.getOrElse(throw new RuntimeException("Unable to get getter for %s.%s".format(clazz, name))).invoke[T](instance)

  def update(instance: AnyRef, value: Any) = setter.getOrElse(throw new RuntimeException("Unable to get setter for %s.%s".format(clazz, name))).invoke[Any](instance, value)

  /**
   * Copies <code>instance</code> setting the new value supplied for this CaseValue
   */
  def copy[T](instance: T, value: Any) = clazz.copy[T](instance, Map(name -> value))

  /**
   * Generates a human readable label for this CaseValue's name
   */
  def label = CaseValue.generateLabel(name)
}

object CaseValue {
  /**
   * Generates a human readable label for this name.
   */
  def generateLabel(name: String) = {
    val b = new StringBuilder
    var p = ' '
    name.foreach {
      case c => {
        if ((c.isUpper || c.isDigit) && (!p.isUpper && !p.isDigit)) {
          b.append(' ')
        }
        b.append(c)
        p = c
      }
    }
    b.toString().capitalize
  }
}