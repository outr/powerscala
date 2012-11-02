package org.powerscala

package object property {
  implicit def propertyToValue[T](property: Property[T]) = property.value

  implicit def propertyBuilderToProperty[T](propertyBuilder: PropertyBuilder[T]) = propertyBuilder.build()
}