package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

case class Sort[T <: Identifiable, F](field: Field[T, F], direction: SortDirection)
