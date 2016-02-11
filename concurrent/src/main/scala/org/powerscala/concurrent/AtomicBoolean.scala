package org.powerscala.concurrent

import java.util.concurrent.atomic.{AtomicBoolean => JavaAtomic}

class AtomicBoolean extends JavaAtomic {
  def apply() = get()

  def attempt[R](f: => R): Option[R] = if (compareAndSet(false, true)) {
    try {
      Some(f)
    } finally {
      set(false)
    }
  } else {
    None
  }
}
