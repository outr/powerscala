package org.powerscala.log.handler

import org.powerscala.event.Intercept
import org.powerscala.log.{LogListener, Level, LogRecord}
import org.powerscala.log.formatter.Formatter
import org.powerscala.log.writer.Writer

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Handler extends LogListener {
  def level: Level

  def publish(record: LogRecord): Unit

  override def receive(record: LogRecord) = {
    if (record.value >= level.value) {
      publish(record)
    }
    Intercept.Continue
  }
}

object Handler {
  def apply(formatter: Formatter, level: Level, writer: Writer) = new SimpleHandler(formatter, level, writer)
}