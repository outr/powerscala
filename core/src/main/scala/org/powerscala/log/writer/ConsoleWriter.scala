package org.powerscala.log.writer

import org.powerscala.log.{Logging, LogRecord}
import org.powerscala.log.formatter.Formatter

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
object ConsoleWriter extends Writer {
  def write(record: LogRecord, formatter: Formatter) = Logging.systemOut.print(formatter.format(record))
}
