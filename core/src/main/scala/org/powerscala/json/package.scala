package org.powerscala

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object json {
  def parse[T](content: String)(implicit manifest: Manifest[T]) = JSONConverter.parse[T](content)(manifest)

  def generate(value: Any): String = JSONConverter.generate(value)
}
