package org.powerscala.property

import backing.StorageBacking
import org.powerscala.Storage

/**
 * @author Matt Hicks <matt@outr.com>
 */
object StorageProperty {
  def apply[T <: S, S](name: String, default: => T, storage: Storage[S])
                      (implicit parent: PropertyParent = null, manifest: Manifest[T]) = {
    val backing = new StorageBacking[T, S](name, default, storage)
    new StandardProperty[T](name, default, backing)(parent, manifest)
  }
}