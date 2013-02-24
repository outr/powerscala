package org.powerscala.property.backing

import org.powerscala.Storage

/**
 * @author Matt Hicks <matt@outr.com>
 */
class StorageBacking[T <: S, S](name: String, default: => T, storage: Storage[S]) extends Backing[T] {
  final def getValue = storage.getOrElse[T](name, default)
  final def setValue(value: T) = storage(name) = value
}