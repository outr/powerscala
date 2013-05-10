package org.powerscala.reflect

import java.lang.reflect.{Modifier, Field}

/**
 * EnhancedField wraps a java.lang.reflect.Field to provide more functionality and easier access.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class EnhancedField protected[reflect](val parent: EnhancedClass, val declaring: EnhancedClass, val javaField: Field) {
  /**
   * The field name.
   */
  def name = javaField.getName

  /**
   * True if this is a native field.
   */
  def isNative = Modifier.isNative(javaField.getModifiers)

  /**
   * True if this is a static field.
   */
  def isStatic = Modifier.isStatic(javaField.getModifiers)

  /**
   * The type of the field.
   */
  lazy val returnType = EnhancedClass(javaField.getType)

  /**
   * Retrieves the value for this field from the supplied instance.
   *
   * @param instance to extract the field value from.
   * @tparam T the return type of this field.
   * @return the value of the field as T.
   */
  def apply[T](instance: Any) = {
    javaField.setAccessible(true)     // Make sure it's accessible
    javaField.get(instance).asInstanceOf[T]
  }

  /**
   * Returns true if <code>c</code> is extended or mixed in to the return type of this field.
   */
  def hasType(c: Class[_]) = c.isAssignableFrom(javaField.getType)

  def signature = s"$name: $returnType"

  def absoluteSignature = s"${declaring.name}.$signature"

  override def toString = absoluteSignature
}
