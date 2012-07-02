package org.powerscala.reflect.doc

import org.powerscala.reflect.EnhancedClass

/**
 * Represents documentation of an EnhancedClass.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class DocumentedClass(name: String, `type` : EnhancedClass, doc: Option[Documentation]) {
  override def toString = if (name != null) {
    name + ": " + `type`.toString
  } else {
    `type`.toString()
  }
}