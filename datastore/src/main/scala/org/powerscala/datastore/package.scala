package org.powerscala

import com.mongodb.util.JSON
import org.powerscala.datastore.converter.DataObjectConverter

package object datastore {
  implicit def v2lazy[T <: Persistable](v: T) = Lazy(v)

  implicit def lazy2v[T <: Persistable](l: Lazy[T]) = l()

  def toJSON(v: Persistable) = DataObjectConverter.toDBValue(v, null).toString

  def fromJSON[T <: Persistable](json: String) = DataObjectConverter.fromDBValue(JSON.parse(json), null).asInstanceOf[T]
}