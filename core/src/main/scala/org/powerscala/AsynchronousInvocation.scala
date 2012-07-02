package org.powerscala

import annotation.tailrec
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * AsynchronousInvocation defines an infrastructure to inject a function to be invoked at a later
 * time by another thread. This works similarly to Actors except these functions are invoked in a
 * specific thread at a specific state.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class AsynchronousInvocation {
  /**
   * The thread for asynchronous invocation.
   */
  @volatile var thread: Thread = _
  private val set = new ConcurrentLinkedQueue[() => Unit]()

  /**
   * Invokes all waiting invocations within this method.
   */
  def invokeNow() = processSet()

  /**
   * Invokes the supplied function later when invokeNow() is called unless it is currently in the correct thread for
   * activation in which case it will be invoked immediately.
   */
  def invokeLater(f: () => Unit) = if (thread == Thread.currentThread()) {
    f()
  } else if (!set.contains(f)) {
    set.add(f)
  }

  @tailrec
  private def processSet(): Unit = {
    if (!set.isEmpty) {
      set.poll() match {
        case null => // Concurrency
        case entry => entry()
      }
      processSet()
    }
  }
}