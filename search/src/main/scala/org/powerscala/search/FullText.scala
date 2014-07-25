package org.powerscala.search

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait FullText {
  final def fullText: String = fullTextFields.flatten.collect {
    case ft: FullText => ft.fullText
    case v if v != null => v.toString
  }.mkString(", ")

  protected def fullTextFields: List[Option[Any]]
}
