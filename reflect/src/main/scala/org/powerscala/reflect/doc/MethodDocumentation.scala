package org.powerscala.reflect.doc

/**
 * MethodDocumentation represents documentation about a specific method.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class MethodDocumentation(args: List[DocumentedClass],
                               returnClass: DocumentedClass,
                               url: String,
                               docs: Option[Documentation])