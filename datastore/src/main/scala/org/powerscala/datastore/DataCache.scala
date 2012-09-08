package org.powerscala.datastore

import org.powerscala.concurrent.Executor

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
abstract class DataCache[T](lifetime: Double)(implicit manifest: Manifest[T]) extends Iterable[T] {
  @volatile
  private var cache: List[T] = Nil
  private var built = false

  Executor.scheduleWithFixedDelay(0.0, lifetime) {
    rebuildCache()
  }

  def rebuildCache() = synchronized {
    cache = query()
    built = true
  }

  def query(): List[T]

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
