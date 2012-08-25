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


import scala.math._
import org.powerscala.{Enumerated, EnumEntry, Precision}
import java.util.Calendar
import java.text.SimpleDateFormat
import collection.mutable.ListBuffer

/**
 * Time represents convenience values and utilities
 * for lengths of time. All values are represented
 * as Doubles of time in seconds.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed case class Time(value: Double, pattern: String) extends EnumEntry[Time] {
  private lazy val dateFormat = new SimpleDateFormat(pattern)

  def format(calendar: Calendar) = dateFormat.format(calendar.getTime)

  /**
   * Parses the Calendar and then returns a Long shortened to the precision of this format in milliseconds.
   */
  def toLong(calendar: Calendar) = {
    val formatted = format(calendar)
    parse(formatted)
  }

  def parse(source: String) = dateFormat.parse(source).getTime

  def parseCalendar(source: String, calendar: Calendar = Calendar.getInstance()) = {
    val time = parse(source)
    calendar.setTimeInMillis(time)
    calendar
  }
}

object Time extends Enumerated[Time] {
  val Second = new Time(1.0, "MM/dd/yyyy hh:mm:ss a")
  val Minute = new Time(60.0 * Second.value, "MM/dd/yyyy hh:mm a")
  val Hour = new Time(60.0 * Minute.value, "MM/dd/yyyy hh a")
  val Day = new Time(24.0 * Hour.value, "MM/dd/yyyy")
  val Week = new Time(7.0 * Day.value, "yyyy w")
  val Month = new Time(30.0 * Day.value, "MM yyyy")
  val Year = new Time(12.0 * Month.value, "yyyy")

  implicit def double2TimeAmount(time: Double) = TimeAmount(time)
  implicit def timeAmount2Double(timeAmount: TimeAmount) = timeAmount.time

  private val reports = new ThreadLocal[Report]

  /**
   * Generates a report for timing of segments of code defined by block names.
   *
   * Use the block(name) method to define segments of application context during the invocation of the function.
   */
  def report(f: => Any): Report = {
    val report = new Report(System.nanoTime())
    reports.set(report)
    try {
      f
    } finally {
      reports.set(null)
    }
    report
  }

  /**
   * @see report
   */
  def block(name: String) = reports.get()(name)

  /**
   * Invokes the wrapped function and returns the time in seconds it took to complete as a Double.
   */
  def elapsed(f: => Any): Double = {
    val time = System.nanoTime
    f
    (System.nanoTime - time) / Precision.Nanoseconds.conversion
  }

  /**
   * Converts time in milliseconds to a short String representation.
   */
  def elapsed(time: Long): String = elapsed(time.toDouble / 1000.0)

  /**
   * Converts time in seconds to a short String representation.
   */
  def elapsed(time: Double): String = {
    val format = "%,.2f"
    var value: Double = time
    var ending = "ms"
    if (time > Year.value) {
      value = time / Year.value
      ending = " years"
    } else if (time > Month.value) {
      value = time / Month.value
      ending = " months"
    } else if (time > Week.value) {
      value = time / Week.value
      ending = " weeks"
    } else if (time > Day.value) {
      value = time / Day.value
      ending = " days"
    } else if (time > Hour.value) {
      value = time / Hour.value
      ending = " hours"
    } else if (time > Minute.value) {
      value = time / Minute.value
      ending = " minutes"
    } else if (time > Second.value) {
      value = time / Second.value
      ending = " seconds"
    }

    String.format(format + ending, value.asInstanceOf[AnyRef])
  }

  /**
   * Converts time in milliseconds to a long String representation.
   */
  def elapsedExact(time: Long) = {
    val b = new StringBuilder()

    var elapsed: Double = time
    var years, months, weeks, days, hours, minutes, seconds = 0

    while (elapsed >= Year.value) {
      years += 1
      elapsed -= Year.value
    }
    while (elapsed >= Month.value) {
      months += 1
      elapsed -= Month.value
    }
    while (elapsed >= Week.value) {
      weeks += 1
      elapsed -= Week.value
    }
    while (elapsed >= Day.value) {
      days += 1
      elapsed -= Day.value
    }
    while (elapsed >= Hour.value) {
      hours += 1
      elapsed -= Hour.value
    }
    while (elapsed >= Minute.value) {
      minutes += 1
      elapsed -= Minute.value
    }
    while (elapsed >= Second.value) {
      seconds += 1
      elapsed -= Second.value
    }

    if (years > 0) b.append(years + "y, ")
    if (months > 0) b.append(months + "m, ")
    if (weeks > 0) b.append(weeks + "w, ")
    if (days > 0) b.append(days + "d, ")
    if (hours > 0) b.append(hours + "h, ")
    if (minutes > 0) b.append(minutes + "m, ")
    if (seconds > 0) b.append(seconds + "s, ")
    b.append(elapsed + "ms")

    b.toString()
  }

  /**
   * Convenience method to sleep a specific amount of time in seconds.
   */
  def sleep(time: Double) = Thread.sleep(millis(time))

  /**
   * Converts time in seconds to milliseconds.
   */
  def millis(time: Double) = round((time * 1000.0).toFloat)

  /**
   * Converts time in nanoseconds to seconds.
   */
  def fromNanos(nanoseconds: Long) = nanoseconds / 1000000000.0

  /**
   * Converts time in milliseconds to seconds.
   */
  def fromMillis(milliseconds: Long) = milliseconds / 1000.0

  /**
   * Waits for <code>condition</code> to return true. This method will wait
   * <code>time</code> (in seconds) for the condition and will return false
   * if the condition is not met within that time. Further, a negative value
   * for <code>time</code> will cause the wait to occur until the condition
   * is true.
   *
   * @param time
   *              The time to wait for the condition to return true.
   * @param precision
   *              The recycle period between checks. Defaults to 0.01s.
   * @param start
   *              The start time in milliseconds since epoc. Defaults to
   *              System.currentTimeMillis.
   * @param errorOnTimeout
   *              If true, throws a java.util.concurrent.TimeoutException upon
   *              timeout. Defaults to false.
   * @param condition
   *              The functional condition that must return true.
   */
  @scala.annotation.tailrec
  def waitFor(time: Double, precision: Double = 0.01, start: Long = System.currentTimeMillis, errorOnTimeout: Boolean = false)(condition: => Boolean): Boolean = {
    val p = round(precision * 1000.0)
    if (!condition) {
      if ((time >= 0.0) && (System.currentTimeMillis - start > millis(time))) {
        if (errorOnTimeout) throw new java.util.concurrent.TimeoutException()
        false
      } else {
        Thread.sleep(p)

        waitFor(time, precision, start, errorOnTimeout)(condition)
      }
    } else {
      true
    }
  }

  def futureCalendar(timeFromNow: Double) = {
    val c = Calendar.getInstance()
    c.setTimeInMillis(System.currentTimeMillis() + millis(timeFromNow))
    c
  }
}

case class TimeAmount(time: Double) {
  def years = TimeAmount(time * Time.Year.value)
  def months = TimeAmount(time * Time.Month.value)
  def weeks = TimeAmount(time * Time.Week.value)
  def days = TimeAmount(time * Time.Day.value)
  def hours = TimeAmount(time * Time.Hour.value)
  def minutes = TimeAmount(time * Time.Minute.value)
  def seconds = TimeAmount(time * Time.Second.value)
  def and(timeAmount: TimeAmount) = TimeAmount(time + timeAmount.time)
}

class Report(_start: Long) {
  protected var _last = _start
  protected var _finished = 0L
  protected var blocks = ListBuffer.empty[(String, Long)]
  protected var absolute = ListBuffer.empty[(String, Long)]

  protected[concurrent] def apply(name: String) = {
    val current = System.nanoTime()
    blocks
    blocks += name -> (current - _last)
    absolute += name -> current
    _last = current
  }

  /**
   * The amount of time the named block took to the beginning of the next block
   */
  def block(name: String) = blocks.find(t => t._1 == name).get._2 / Precision.Nanoseconds.conversion

  /**
   * The amount of time the named block took from the beginning of the report
   */
  def elapsed(name: String) = (when(name) - _start) / Precision.Nanoseconds.conversion

  /**
   * The actual time in nanoseconds when the block started
   */
  def when(name: String) = absolute.find(t => t._1 == name).get._2

  override def toString = {
    val b = new StringBuilder
    blocks.foreach {
      case (name, length) => {
        b.append("%s - Block: %ss, Elapsed: %ss\r\n".format(name, block(name), elapsed(name)))
      }
    }
    b.append()
    b.toString()
  }
}