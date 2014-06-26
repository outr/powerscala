package org.powerscala.log

import org.powerscala.Priority
import org.powerscala.event.{Listener, ListenMode, Intercept}

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait LogListener extends Listener[LogRecord, Intercept] {
  override val name = "logger"
  override def priority = Priority.Normal
  override def eventClass = classOf[LogRecord]
  override val modes = List(ListenMode.Standard)
}