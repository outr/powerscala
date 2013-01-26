package org.powerscala.log.handler

import org.powerscala.log.{Level, LogRecord}
import org.powerscala.log.formatter.Formatter
import org.powerscala.log.writer.Writer

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Handler {
  def level: Level

  def publish(record: LogRecord): Unit
}

object Handler {
  def apply(formatter: Formatter, level: Level, writer: Writer) = new SimpleHandler(formatter, level, writer)
}