package org.powerscala.log

import formatter.Formatter
import handler.Handler
import annotation.tailrec
import writer.{Writer, ConsoleWriter}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class Logger private(val name: String, val parentName: String, val level: Level, val handlers: List[Handler] = Nil) {
  def parent = if (parentName == null) {
    null
  } else {
    Logger(parentName)
  }

  def withParentName(parentName: String) = new Logger(name, parentName, level, handlers)

  def withLevel(level: Level) = new Logger(name, parentName, level, handlers)

  def withHandler(handler: Handler): Logger = new Logger(name, parentName, level, (handler :: handlers.reverse).reverse)

  def withHandler(formatter: Formatter = Formatter.Default,
                  level: Level = Level.Trace,
                  writer: Writer = ConsoleWriter): Logger = withHandler(Handler(formatter, level, writer))

  def withoutHandlers = new Logger(name, parentName, level, Nil)

  def isLevelEnabled(level: Level): Boolean = hasLevel(level, handlers) || (parentName != null && parent.isLevelEnabled(level))

  @tailrec
  private def hasLevel(level: Level, list: List[Handler]): Boolean = {
    if (this.level.value <= level.value) {
      if (list.isEmpty) {
        false
      } else {
        if (list.head.level.value <= level.value) {
          true
        } else {
          hasLevel(level, list.tail)
        }
      }
    } else {
      false
    }
  }

  def log(record: LogRecord): Unit = {
    process(record, handlers)
    if (parent != null) {
      parent.log(record)
    }
  }

  @tailrec
  private def process(record: LogRecord, list: List[Handler]): Unit = {
    if (list.nonEmpty) {
      val handler = list.head
      if (isLevelEnabled(record.level)) {
        handler.publish(record)
      }
      process(record, list.tail)
    }
  }
}

object Logger {
  private var loggers = Map.empty[String, Logger]

  def Root = apply("root")

  configure("root") {
    case l => l.withHandler(Handler(Formatter.Default, Level.Trace, ConsoleWriter))
  }

  def apply(name: String): Logger = loggers.getOrElse(name, Root)

  def configure(name: String)(f: Logger => Logger) = synchronized {
    val parentName = if (name == "root") {
      null
    } else {
      "root"
    }
    val original = loggers.getOrElse(name, new Logger(name = name, parentName = parentName, level = Level.Info, handlers = Nil))
    val configured = f(original)
    loggers += name -> configured
  }
}