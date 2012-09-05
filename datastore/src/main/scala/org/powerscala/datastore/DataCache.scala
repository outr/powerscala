package org.powerscala.datastore

import org.powerscala.concurrent.Executor

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class DataCache[T <: Identifiable](datastore: Datastore, name: String = null, lifetime: Double)
                                  (implicit manifest: Manifest[T]) extends Iterable[T] {
  @volatile
  private var cache: List[T] = Nil
  private var built = false

  Executor.scheduleWithFixedDelay(0.0, lifetime) {
    rebuildCache()
  }

  def rebuildCache() = synchronized {
    datastore {
      case session => cache = query(session.collection[T](name)(manifest))
    }
    built = true
  }

  def query(collection: DatastoreCollection[T]): List[T] = {
    collection.iterator.toList
  }

  def iterator = {
    waitForBuild()
    cache.iterator
  }

  def waitForBuild() = {
    while (!built) {
      Thread.sleep(10)
    }
  }
}
