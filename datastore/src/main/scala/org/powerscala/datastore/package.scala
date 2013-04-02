package org.powerscala

import com.mongodb.util.JSON
import org.powerscala.datastore.converter.DataObjectConverter

package object datastore {
  import language.implicitConversions

  implicit def v2lazy[T <: Identifiable](v: T)(implicit manifest: Manifest[T]) = Lazy(v)

  implicit def lazy2v[T <: Identifiable](l: Lazy[T]) = l()

  def toJSON(v: Identifiable) = DataObjectConverter.toDBValue(v, null).toString

  def fromJSON[T <: Identifiable](json: String) = DataObjectConverter.fromDBValue(JSON.parse(json), null).asInstanceOf[T]
}