package org.powerscala.datastore.query


case class Sort[T, F](field: Field[T, F], direction: SortDirection)
