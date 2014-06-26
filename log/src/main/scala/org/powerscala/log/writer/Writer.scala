package org.powerscala.log.writer

import org.powerscala.log.LogRecord
import org.powerscala.log.formatter.Formatter

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Writer {
  def write(record: LogRecord, formatter: Formatter): Unit
}
