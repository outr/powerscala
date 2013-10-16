package org.powerscala

import java.util.concurrent.atomic.AtomicBoolean
import reflect._

/**
 * Disposed should only be applied to objects, not classes.
 *
 * Disposed() should be invoked at application shutdown to dispose all implementing traits.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Disposable {
  def dispose(): Unit
}

object Disposable {
  private val disposed = new AtomicBoolean(false)

  /**
   * This method should be called at shutdown of the application to allow disposal of all Disposed implementing
   * objects. This may be called more than once but will only ever invoke Disposed.dispose() once per runtime.
   */
  def apply() = if (disposed.compareAndSet(false, true)) {
    val c: EnhancedClass = classOf[Disposable]
    c.subTypes.foreach {
      case clazz => if (clazz.isCompanion) {
        val d = clazz.instance.get.asInstanceOf[Disposable]
        d.dispose()
      } else {
        throw new ClassCastException("%s is not a object and cannot be used for Disposed")
      }
    }
  }
}