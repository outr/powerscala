package org.powerscala.reflect

import java.lang.reflect.Modifier

/**
 * CaseValue represents a value on a case class.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class CaseValue(name: String, valueType: EnhancedClass, parentClass: EnhancedClass) {
  lazy val getter = parentClass.method(name)
  lazy val setter = parentClass.methods.find(m => m.name == "%s_$eq".format(name))

  private lazy val field = parentClass.javaClass.getDeclaredField(name)

  def isMutable = setter != None

  def isTransient = Modifier.isTransient(field.getModifiers)

  def apply[T](instance: AnyRef) = getter.getOrElse(throw new RuntimeException("Unable to get getter for %s.%s".format(parentClass, name))).invoke[T](instance)

  def update(instance: AnyRef, value: Any) = setter.getOrElse(throw new RuntimeException("Unable to get setter for %s.%s".format(parentClass, name))).invoke[Any](instance, value)

  /**
   * Copies <code>instance</code> setting the new value supplied for this CaseValue
   */
  def copy[T](instance: T, value: Any) = parentClass.copy[T](instance, Map(name -> value))

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
        if (b.length > 1 && (p.isUpper || p.isDigit) && (!c.isUpper && !c.isDigit)) {
          b.insert(b.length - 1, ' ')
        }
        b.append(c)
        p = c
      }
    }
    b.toString().capitalize
  }
}