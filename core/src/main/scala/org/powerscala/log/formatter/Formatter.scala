package org.powerscala.log.formatter

import org.powerscala.log.LogRecord

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  val Default = FormatterBuilder().date().string(" [").threadName.string("] ").levelPaddedRight.string(" ").classNameAbbreviated.string(" - ").message.newLine
  val Advanced = FormatterBuilder().date().string(" [").threadName.string("] ").levelPaddedRight.string(" ").classNameAbbreviated.string(".").methodName.string(":").lineNumber.string(" - ").message.newLine
}