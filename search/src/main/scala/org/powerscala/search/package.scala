package org.powerscala

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object search {
  implicit class IndexableOption(option: Option[Indexable]) {
    def fields = option match {
      case Some(io) => io.indexableFields
      case None => Nil
    }
  }
}