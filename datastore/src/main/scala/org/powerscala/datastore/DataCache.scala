package org.powerscala.datastore

import org.powerscala.concurrent.Executor

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class DataCache[T <: Identifiable](datastore: Datastore, name: String = null, lifetime: Double)
                                  (builder: DatastoreCollection[T] => Seq[T] = (c: DatastoreCollection[T]) => c.iterator.toList)
                                  (implicit manifest: Manifest[T]) extends Iterable[T] {
  @volatile
  private var cache: Seq[T] = Nil
  private var built = false

  Executor.scheduleWithFixedDelay(0.0, lifetime) {
    rebuildCache()
  }

  def rebuildCache() = synchronized {
    datastore {
      case session => cache = builder(session.collection[T](name)(manifest))
    }
    built = true
  }

  def iterator = cache.iterator

  def waitForBuild() = {
    while (!built) {
      Thread.sleep(10)
    }
  }
}
