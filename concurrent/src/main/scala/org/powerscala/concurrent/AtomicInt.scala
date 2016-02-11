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

import java.util.concurrent.atomic.AtomicInteger
import annotation.tailrec

/**
 * AtomicInt extends the functionality of AtomicInteger to provide additional convenience functionality.
 */
class AtomicInt(initial: Int) extends AtomicInteger(initial) {
  def apply() = get()

  /**
   * Modifies the value atomicly without locking if the resulting value of the function is Some.
   */
  @tailrec
  final def modify(f: Int => Option[Int]): Boolean = {
    val current = get()
    f(current) match {
      case Some(v) => if (compareAndSet(current, v)) {
        true
      }
      else {
        modify(f)
      }
      case None => false
    }
  }

  /**
   * Increments and returns the new value
   */
  def ++ = incrementAndGet()

  /**
   * Decrements and returns the new value
   */
  def -- = decrementAndGet()

  /**
   * Adds the value and returns the new value
   */
  def +=(value: Int) = addAndGet(value)

  /**
   * Subtracts the value and returns the new value
   */
  def -=(value: Int) = addAndGet(-value)

  /**
   * Increments the value if the current value is less than the max value supplied.
   *
   * This method is thread-safe without locking.
   */
  def incrementIfLessThan(max: Int) = modify((value: Int) => {
    if (value < max) {
      Some(value + 1)
    }
    else {
      None
    }
  })

  /**
   * Decrements the value if the current value is greater than the max value supplied.
   *
   * This method is thread-safe without locking.
   */
  def decrementIfGreaterThan(min: Int) = modify((value: Int) => {
    if (value > min) {
      Some(value - 1)
    }
    else {
      None
    }
  })
}