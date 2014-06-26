package org.powerscala.log

import java.io.OutputStream

/**
 * @author Matt Hicks <matt@outr.com>
 */
class LoggingOutputStream(override val loggerName: String, level: Level) extends OutputStream with Logging {
  private val b = new StringBuilder

  // TODO: override other write methods to make it more efficient

  def write(i: Int) = {
    val c = i.toChar
    if (c == '\n' || c == '\r') {
      if (b.length > 0) {
        log(level, b.toString())
        b.clear()
      }
    } else {
      b.append(c)
    }
  }
}