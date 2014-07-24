package org.powerscala.search

import org.apache.lucene.document.Field

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Indexable {
  def indexableFields: List[Field]
}