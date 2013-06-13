package org.powerscala

import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class FilteredFields[T](parent: AnyRef)(implicit manifest: Manifest[T]) {
  private lazy val parentClass = parent.getClass
  private lazy val parentFields = parentClass.fields.collect {
    case f if (!f.isStatic && f.hasType(manifest.runtimeClass)) => f
  }
  private lazy val nameMap = parentFields.map(field2Entry _).toMap
  private lazy val valueMap = nameMap.map(t => t._2 -> t._1)
  def values = valueMap.keys

  def apply(name: String, caseSensitive: Boolean = false) = get(name, caseSensitive).getOrElse(null)

  def get(name: String, caseSensitive: Boolean = false) = if (caseSensitive) {
    nameMap.get(name)
  } else {
    values.find(t => name.equalsIgnoreCase(nameOf(t)))
  }

  def nameOf(t: T) = valueMap.getOrElse(t, null)

  private def field2Entry(f: EnhancedField) = {
    val name = f.name
    val value = f[T](parent, computeIfLazy = true)
    name -> value
  }
}
