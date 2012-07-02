package org.powerscala.datastore.query

import org.powerscala.datastore.Persistable

case class Sort[T <: Persistable, F](field: Field[T, F], direction: SortDirection)
