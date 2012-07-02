package org.powerscala.datastore.query

import org.powerscala.datastore.Persistable

case class Filter[T <: Persistable, F](field: Field[T, F], operator: Operator, value: F)
