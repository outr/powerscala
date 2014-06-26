package org.powerscala.log.handler

import org.powerscala.log.formatter.Formatter
import org.powerscala.log.writer.Writer
import org.powerscala.log.{Level, LogRecord}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class SimpleHandler(val formatter: Formatter,
                    val level: Level,
                    writer: Writer) extends Handler {
  def publish(record: LogRecord) = {
    writer.write(record, formatter)
  }
}