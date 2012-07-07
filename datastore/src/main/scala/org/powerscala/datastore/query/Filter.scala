package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

case class Filter[T <: Identifiable, F](field: Field[T, F], operator: Operator, value: F)
