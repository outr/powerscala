package org.powerscala.concurrent

import org.powerscala.log.Logging
import org.powerscala.concurrent.Time._
import org.powerscala.{Disposable, Updatable}

/**
 * Temporal represents an object that only exists for a specified period of time before being disposed. Regular
 * check-ins are required to keep the object from timing out.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait Temporal extends Updatable with Logging with Disposable {
  val created = System.currentTimeMillis()

  @volatile private var lastCheckIn = created
  @volatile private var _disposed = false

  final def lifetime = fromMillis(System.currentTimeMillis() - created)
  final def stale = fromMillis(System.currentTimeMillis() - lastCheckIn)
  final def disposed = _disposed

  def timeout: Double

  protected def checkIn() = lastCheckIn = System.currentTimeMillis()

  override def update(delta: Double) {
    val elapsed = stale
    if (elapsed > timeout) {    // Temporal has timed out
      debug(s"${getClass.getSimpleName} ($this) has timed out; Elapsed: $elapsed, Timeout: $timeout")
      _disposed = true
      dispose()
    } else if (disposed) {
      throw new RuntimeException(s"${getClass.getSimpleName} ($this) has already been disposed, it cannot be updated anymore!")
    } else {
      super.update(delta)
    }
  }
}
