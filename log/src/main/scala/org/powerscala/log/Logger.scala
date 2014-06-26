package org.powerscala.log

import java.io.PrintStream

import org.powerscala.event.{Listenable, Intercept, EventState}
import org.powerscala.event.processor.{OptionProcessor, EventProcessor}
import org.powerscala.log.formatter.Formatter
import org.powerscala.log.handler.Handler
import org.powerscala.log.writer.{ConsoleWriter, Writer}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Logger private(val loggerName: String) extends EventProcessor[LogRecord, Intercept, Intercept] with Listenable {
  val name = "logger"

  override def listenable = this

  private var _parent: Option[Logger] = if (loggerName == "root") None else Some(Logger.Root)
  private var _multiplier: Double = 1.0
  private var _detailed: Boolean = false

  val eventManifest = implicitly[Manifest[LogRecord]]

  def parent = _parent
  def parent_=(parent: Option[Logger]) = _parent = parent

  def multiplier = _multiplier
  def multiplier_=(value: Double) = _multiplier = value

  def detailed = _detailed
  def detailed_=(detailed: Boolean) = _detailed = detailed

  protected def handleListenerResponse(value: Intercept, state: EventState[LogRecord]) = if (value == Intercept.Stop) {
    state.stopPropagation()
  }

  protected def responseFor(state: EventState[LogRecord]) = if (state.isStopPropagation) {
    Intercept.Stop
  } else {
    Intercept.Continue
  }

  def trace(message: => Any): Unit = log(Level.Trace, message)
  def debug(message: => Any): Unit = log(Level.Debug, message)
  def info(message: => Any): Unit = log(Level.Info, message)
  def warn(message: => Any): Unit = log(Level.Warn, message)
  def error(message: => Any): Unit = log(Level.Error, message)

  def warn(message: => Any, t: Throwable): Unit = {
    log(Level.Warn, message)
    log(Level.Warn, t)
  }
  def error(message: => Any, t: Throwable): Unit = {
    log(Level.Error, message)
    log(Level.Error, t)
  }

  def log(level: Level, message: => Any): Unit = {
    val record = if (detailed) {
      LogRecord.trace(level, () => message, loggerName)
    } else {
      LogRecord(level, () => message, loggerName)
    }
    log(record)
  }

  def log(record: LogRecord): Unit = {
    fire(record.boost(multiplier)) match {
      case Intercept.Continue => parent match {
        case Some(parentLogger) => parentLogger.log(record)
        case None => // Stopped logging
      }
    }
  }

  def +=(handler: Handler) = listenable.listeners += handler
  def -=(handler: Handler) = listenable.listeners -= handler
  def addHandler(writer: Writer, level: Level = Level.Info, formatter: Formatter = Formatter.Default) = {
    val handler = Handler(formatter, level, writer)
    this += handler
    handler
  }
}

object Logger extends Listenable {
  val systemOut = System.out
  val systemErr = System.err

  val stringify = new OptionProcessor[Any, String]("stringify")
  stringify.on {
    case null => Some("")
    case s: String => Some(s)
    case t: Throwable => Some(throwable2String(t))
    case m => Some(m.toString)
  }

  private var loggers = Map.empty[String, Logger]

  lazy val Root = {
    val l = apply("root")
    l += DefaultRootHandler
    l
  }
  lazy val DefaultRootHandler = Handler(Formatter.Default, Level.Info, ConsoleWriter)

  def apply(name: String) = synchronized {
    loggers.get(name) match {
      case Some(logger) => logger
      case None => {
        val logger = new Logger(name)
        loggers += name -> logger
        logger
      }
    }
  }

  /**
   * Enables or disables logging of System.out and System.err to go through the logging system
   *
   * @param out true if System.out should redirect to info logging. Defaults to true.
   * @param err true if System.err should redirect to error logging. Defaults to true.
   */
  def configureSystem(out: Boolean = true, err: Boolean = true) = {
    val os = if (out) new PrintStream(new LoggingOutputStream("System.out", Level.Info)) else systemOut
    val es = if (err) new PrintStream(new LoggingOutputStream("System.err", Level.Error)) else systemErr
    System.setOut(os)
    System.setErr(es)
  }

  def throwable2String(t: Throwable, primaryCause: Boolean = true): String = {
    val b = new StringBuilder
    if (!primaryCause) {
      b.append("Caused by: ")
    }
    b.append(t.getClass.getName)
    if (t.getLocalizedMessage != null) {
      b.append(": ")
      b.append(t.getLocalizedMessage)
    }
    b.append(System.lineSeparator())
    writeStackTrace(b, t.getStackTrace)
    if (t.getCause != null) {
      b.append(throwable2String(t.getCause, primaryCause = false))
    }
    b.toString()
  }

  private def writeStackTrace(b: StringBuilder, elements: Array[StackTraceElement]): Unit = {
    if (elements.nonEmpty) {
      val head = elements.head
      b.append("\tat ")
      b.append(head.getClassName)
      b.append('.')
      b.append(head.getMethodName)
      b.append('(')
      if (head.getLineNumber == -2) {
        b.append("Native Method")
      } else {
        b.append(head.getFileName)
        if (head.getLineNumber > 0) {
          b.append(':')
          b.append(head.getLineNumber)
        }
      }
      b.append(')')
      b.append(System.lineSeparator())
      writeStackTrace(b, elements.tail)
    }
  }
}