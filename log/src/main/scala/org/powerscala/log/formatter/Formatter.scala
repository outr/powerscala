package org.powerscala.log.formatter

import org.powerscala.log.LogRecord

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Formatter {
  def format(record: LogRecord): String
}

object Formatter {
  val Default = FormatterBuilder.parse("${date} [${threadName}] ${levelPaddedRight} ${classNameAbbreviated} - ${message}${newLine}")
  val Advanced = FormatterBuilder().date().string(" [").threadName.string("] ").levelPaddedRight.string(" ").classNameAbbreviated.string(".").methodName.string(":").lineNumber.string(" - ").message.newLine
}