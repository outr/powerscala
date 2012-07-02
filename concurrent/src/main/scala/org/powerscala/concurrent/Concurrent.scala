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

import actors.DaemonActor

/**
 * Concurrent trait mixes in convenience functionality to invoke functions asynchronously and
 * concurrently.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Concurrent {
  private lazy val actor = createActor()

  private def createActor() = {
    val a = new DaemonActor() {
      def act() {
        loop {
          react {
            case invocation: Function0[_] => invocation()
          }
        }
      }
    }
    a.start()
  }

  /**
   * Asynchronously invokes the supplied function. All asynchronous calls are made in sequence outside of the
   * calling thread.
   */
  def asynchronous(f: => Unit) = {
    actor ! f
  }

  /**
   * Concurrently invokes the supplied function. This is entirely non-blocking and will be executed against a backing
   * thread-pool.
   */
  def concurrent(f: => Unit) = {
    Executor.execute(new Runnable() {
      def run() = f
    })
  }
}