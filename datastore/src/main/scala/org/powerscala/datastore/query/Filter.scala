package org.powerscala.datastore.query

import org.powerscala.datastore.Identifiable

case class Filter[T <: Identifiable](field: Field[T, _], operator: Operator, value: Any)
