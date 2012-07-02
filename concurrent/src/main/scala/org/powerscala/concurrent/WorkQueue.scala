/*
 * Copyright (c) 2011 PowerScala
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'PowerScala' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.powerscala.concurrent

import java.util.concurrent.ConcurrentLinkedQueue
import annotation.tailrec

/**
 * WorkQueue provides a backing concurrent queue to store a backlog of work to be done and can be invoked arbitrarily
 * based on the needs of the implementation.
 *
 * Enqueueing work can be done via the companion object: WorkQueue.enqueue(workQueue, function).
 *
 * NOTE: Implementations must make calls to doWork() and/or doAllWork() or the queue will never be processed.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait WorkQueue {
  private lazy val queue = new ConcurrentLinkedQueue[() => Any]

  /**
   * Polls the queue for the first-in unit of work to be done and then executes it.
   *
   * NOTE: If the unit of work throws an exception it will be propagated to the caller.
   *
   * @return true if work was done, false if the queue is empty
   */
  protected final def doWork() = {
    if (!queue.isEmpty) {
      queue.poll() match {
        case null => false
        case f => {
          f()
          true
        }
      }
    } else {
      false
    }
  }

  /**
   * Continues to poll the queue for the first-in unit of work to be done and then executes it until there is no more
   * work left in the queue.
   *
   * NOTE: If the unit of work throws an exception it will be propagated to the caller.
   */
  @tailrec
  protected final def doAllWork(): Unit = {
    if (doWork()) {
      doAllWork()
    }
  }
}

object WorkQueue {
  /**
   * Enqueue some work to be done in the future by <code>workQueue</code>
   */
  def enqueue(workQueue: WorkQueue, f: () => Any) = workQueue.queue.add(f)
}