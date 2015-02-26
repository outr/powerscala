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
import org.powerscala.enum.{Enumerated, EnumEntry}
import java.util.Calendar
import java.text.SimpleDateFormat
import collection.mutable.ListBuffer
import org.powerscala.Precision

/**
 * Time represents convenience values and utilities
 * for lengths of time. All values are represented
 * as Doubles of time in seconds.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed case class Time(value: Double, pattern: String) extends EnumEntry {
  private lazy val dateFormat = new SimpleDateFormat(pattern)

  def format(calendar: Calendar) = dateFormat.format(calendar.getTime)

  def millis = Time.millis(value)

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
  import language.implicitConversions

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
    val report = reportStart()
    try {
      f
    } finally {
      reportFinish()
    }
    report
  }

  def reportStart() = {
    val report = new Report(System.nanoTime())
    reports.set(report)
    report
  }

  def reportFinish() = {
    val report = reports.get()
    reports.set(null)
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

  def elapsedReturn[R](f: R): (R, Double) = {
    val time = System.nanoTime()
    val result: R = f
    result -> fromNanos(System.nanoTime - time)
  }

  class Counters(logger: String => Unit) {
    var map = Map.empty[String, Int]

    def increment(name: String) = {
      val value = map.getOrElse(name, 0)
      map += name -> (value + 1)
    }

    def log() = if (map.nonEmpty) {
      val s = map.map {
        case (n, v) => s"$n = $v"
      }.mkString(", ")
      logger(s)
    }
  }
  private val counters = new ThreadLocal[Counters]
  def withCounters[R](logEvery: Double = 1.0, logger: String => Unit)(f: => R): R = {
    val c = new Counters(logger)
    counters.set(c)
    try {
      val scheduled = Executor.scheduleWithFixedDelay(logEvery, logEvery) {
        c.log()
      }
      try {
        f
      } finally {
        scheduled.cancel(false)
      }
    } finally {
      c.log()
      counters.remove()
    }
  }
  def hasCounter = counters.get() != null
  def increment(name: String) = {
    val c = counters.get()
    c.increment(name)
  }

  /**
   * Converts time in milliseconds to an Elapsed instance.
   */
  def elapsed(time: Long): Elapsed = elapsed(time.toDouble / 1000.0)

  /**
   * Converts time in seconds to an Elapsed instance.
   */
  def elapsed(time: Double) = Elapsed(time)

  /**
   * Convenience method to sleep a specific amount of time in seconds.
   */
  def sleep(time: Double) = Thread.sleep(millis(time))

  /**
   * Converts time in seconds to milliseconds.
   */
  def millis(time: Double) = round(time * 1000.0)

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

  def toMilliseconds = Time.millis(time)
}

class Report(_start: Long) {
  protected var _last = _start
  protected var _finished = 0L
  protected var blocks = ListBuffer.empty[(String, Long)]
  protected var absolute = ListBuffer.empty[(String, Long)]

  def apply(name: String) = {
    val current = System.nanoTime()
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
    b.toString()
  }
}

case class Elapsed(time: Double) {
  val (days, hours, minutes, seconds, milliseconds) = evaluate()

  private def evaluate() = {
    val days = (time / Time.Day.value).toInt
    var t = time - (days * Time.Day.value)
    val hours = (time / Time.Hour.value).toInt
    t -= hours * Time.Hour.value
    val minutes = (t / Time.Minute.value).toInt
    t -= minutes * Time.Minute.value
    val seconds = t.toInt
    t -= seconds
    val milliseconds = (t * 1000).toInt
    (days, hours, minutes, seconds, milliseconds)
  }

  lazy val shorthand = {
    import Time._
    var value: Double = time
    val ending = if (time > Year.value) {
      value = time / Year.value
      "year"
    } else if (time > Month.value) {
      value = time / Month.value
      "month"
    } else if (time > Week.value) {
      value = time / Week.value
      "week"
    } else if (time > Day.value) {
      value = time / Day.value
      "day"
    } else if (time > Hour.value) {
      value = time / Hour.value
      "hour"
    } else if (time > Minute.value) {
      value = time / Minute.value
      "minute"
    } else if (time > Second.value) {
      value = time / Second.value
      "second"
    } else {
      "ms"
    }
    val round = math.round(value)
    val s = if (round != 1 && ending != "ms") "s" else ""

    s"$round $ending$s"
  }

  override def toString = {
    var l = ListBuffer.empty[String]
    if (days > 0) l += s"$days days"
    if (hours > 0) l += s"$hours hours"
    if (minutes > 0) l += s"$minutes minutes"
    if (seconds > 0) l += s"$seconds seconds"
    if (milliseconds > 0) l += s"$milliseconds milliseconds"
    if (l.nonEmpty) {
      l.mkString(", ")
    } else {
      "0 milliseconds"
    }
  }
}