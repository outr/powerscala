package org.powerscala

import java.util.concurrent.atomic.AtomicBoolean
import reflect._

/**
 * Initialize should only be applied to objects, not classes.
 *
 * Initialize() should be invoked at application startup to initialize all implementing traits.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Initialized {
  def init(): Unit
}

object Initialized {
  private val initted = new AtomicBoolean(false)

  /**
   * This method should be called at startup of the application to allow initialization of all Initialized implementing
   * objects. This may be called more than once but will only ever invoke Initialized.init() once per runtime.
   */
  def apply() = if (initted.compareAndSet(false, true)) {
    val c: EnhancedClass = classOf[Initialized]
    c.subTypes.foreach {
      case clazz => if (clazz.isCompanion) {
        val initialized = clazz.instance.get.asInstanceOf[Initialized]
        initialized.init()
      } else {
        throw new ClassCastException("%s is not a object and cannot be used for Initialized")
      }
    }
  }
}