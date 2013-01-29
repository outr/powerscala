package org.powerscala.log

import formatter.Formatter
import handler.Handler
import annotation.tailrec
import writer.{Writer, ConsoleWriter}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class Logger private(val name: String, val parentName: String, val handlers: List[Handler] = Nil) {
  def parent = if (parentName == null) {
    null
  } else {
    Logger(parentName)
  }

  def withParentName(parentName: String) = new Logger(name, parentName, handlers)

  def withHandler(handler: Handler): Logger = new Logger(name, parentName, (handler :: handlers.reverse).reverse)

  def withHandler(formatter: Formatter = Formatter.Default,
                  level: Level = Level.Info,
                  writer: Writer = ConsoleWriter): Logger = withHandler(Handler(formatter, level, writer))

  def withoutHandlers = new Logger(name, parentName, Nil)

  def isLevelEnabled(level: Level): Boolean = hasLevel(level, handlers) || (parentName != null && parent.isLevelEnabled(level))

  @tailrec
  private def hasLevel(level: Level, list: List[Handler]): Boolean = {
    if (list.isEmpty) {
      false
    } else {
      if (list.head.level.value <= level.value) {
        true
      } else {
        hasLevel(level, list.tail)
      }
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
    case l => l.withHandler(Handler(Formatter.Default, Level.Info, ConsoleWriter))
  }

  def apply(name: String): Logger = loggers.getOrElse(name, Root)

  def configure(name: String)(f: Logger => Logger) = synchronized {
    val parentName = if (name == "root") {
      null
    } else {
      "root"
    }
    val original = loggers.getOrElse(name, new Logger(name = name, parentName = parentName, handlers = Nil))
    val configured = f(original)
    loggers += name -> configured
  }
}