package org.powerscala.reflect.doc


/**
 * Implementations of DocumentationReflection provide functionality to introspect the JavaDoc /
 * ScalaDoc for a Class.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DocMapper {
  def apply(c: Class[_]): DocumentationReflection
}