package org.powerscala.search

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FullText {
  final def fullText: String = fullTextFields.flatten.map {
    case ft: FullText => ft.fullText
    case v => v.toString
  }.mkString(", ")

  protected def fullTextFields: List[Option[Any]]
}
