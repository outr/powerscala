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

import java.lang.Thread
import java.util.concurrent.{TimeUnit, ThreadFactory, Callable, Executors}

/**
 * Executor is a light-weight wrapper around a Java ExecutorService backred by a cached thread-pool.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object Executor {
  private lazy val threadFactory = new ThreadFactory {
    def newThread(r: Runnable) = {
      val t = new Thread(r)
      t.setDaemon(true)
      t
    }
  }
  private lazy val executor = Executors.newCachedThreadPool(threadFactory)
  private lazy val scheduler = Executors.newScheduledThreadPool(2, threadFactory)

  /**
   * Invokes the function on the ExecutorService asynchronously returning a Future[T] with the return value.
   *
   * @return Future[T]
   */
  def future[T](f: () => T) = executor.submit(new C[T](f))

  /**
   * Executes the function on the ExecutorService asynchronously.
   */
  def execute[T](f: () => T): Unit = execute(new R(f))

  /**
   * Synonymous to the execute method apart from the difference in function signature.
   */
  def invoke[T](f: => T): Unit = execute(() => f)

  /**
   * Synonymous to the future method apart from the difference in function signature.
   */
  def invokeFuture[T](f: => T) = executor.submit(new C[T](() => f))

  /**
   * Executes a Runnable in the future.
   */
  def execute(r: Runnable): Unit = executor.execute(r)

  // TODO: the following methods need to fire off to future and execute to free up "scheduler" and a new Future needs to
  // be created to support it.

  def schedule[T](delay: Double)(f: => T) = scheduler
      .schedule(new C[T](() => f), Time.millis(delay), TimeUnit.MILLISECONDS)

  def scheduleAtFixedRate[T](initialDelay: Double, period: Double)(f: => T) = {
    scheduler.scheduleAtFixedRate(new R[T](() => f), Time.millis(initialDelay), Time.millis(period),
      TimeUnit.MILLISECONDS)
  }

  def scheduleWithFixedDelay[T](initialDelay: Double, delay: Double)(f: => T) = {
    scheduler
        .scheduleWithFixedDelay(new R[T](() => f), Time.millis(initialDelay), Time.millis(delay),
      TimeUnit.MILLISECONDS)
  }

  class C[T](f: () => T) extends Callable[T] {
    def call = f()
  }

  class R[T](f: () => T) extends Runnable {
    def run = f()
  }

}