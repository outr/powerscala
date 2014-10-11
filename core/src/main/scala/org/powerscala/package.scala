package org

import scala.annotation.tailrec

package object powerscala {
  import language.implicitConversions

  type Interceptor[T] = (T, T, T) => Option[T]

  implicit def double2Float(value: Double) = value.toFloat

  implicit def t2p2(t: Throwable) = new PowerThrowable(t)

  def ignoreExceptions(f: => Any) = try {
    f
  } catch {
    case t: Throwable => // Ignore
  }
}

class PowerThrowable(t: Throwable) {
  def rootCause = findRootCause(t)

  @tailrec
  private def findRootCause(t: Throwable): Throwable = if (t.getCause == null) {
    t
  } else {
    findRootCause(t.getCause)
  }
}