package org.powerscala.search

import com.spatial4j.core.shape.Shape
import org.apache.lucene.document.Field

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class DocumentUpdate(fields: List[Field], shapes: List[Shape])

object DocumentUpdate {
  def apply(fields: Field*): DocumentUpdate = DocumentUpdate(fields.toList, Nil)
}