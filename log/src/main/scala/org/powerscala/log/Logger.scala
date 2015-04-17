package org.powerscala.log

import java.io.{File, PrintStream}

import org.powerscala.event.{Listenable, Intercept, EventState}
import org.powerscala.event.processor.{OptionProcessor, EventProcessor}
import org.powerscala.log.formatter.Formatter
import org.powerscala.log.handler.Handler
import org.powerscala.log.writer.{FileWriter, ConsoleWriter, Writer}

import scala.annotation.tailrec

/**
 * Logger is used to receive LogRecords, fire to handlers, and propagate them up to parent Loggers. A Logger always
 * defaults Logger.Root as the parent.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class Logger private(val loggerName: String) extends EventProcessor[LogRecord, Intercept, Intercept] with Listenable {
  val name = "logger"

  override def listenable = this

  private var _parent: Option[Logger] = if (loggerName == "root") None else Some(Logger.Root)
  private var _multiplier: Double = 1.0
  private var _detailed: Boolean = false

  val eventManifest = implicitly[Manifest[LogRecord]]

  /**
   * This Logger's parent Logger.
   */
  def parent = _parent

  /**
   * Set the parent Logger. If this is set to None then propagation will stop with this Logger.
   *
   * Defaults to Logger.Root.
   */
  def parent_=(parent: Option[Logger]) = _parent = parent

  /**
   * The multiplier boosts the value of LogRecords received at this Logger and retains the boosted value as it
   * propagates up hierarchically.
   */
  def multiplier = _multiplier

  /**
   * The multiplier boosts the value of LogRecords received at this Logger and retains the boosted value as it
   * propagates up hierarchically.
   *
   * Defaults to 1.0.
   */
  def multiplier_=(value: Double) = _multiplier = value

  /**
   * Boosts the muliplier (multiplies multiplier by the supplied value).
   */
  def boost(value: Double) = multiplier = multiplier * value

  /**
   * Flag defining whether detailed LogRecords should be created for log records created from this logger.
   *
   * Detailed LogRecords include information on the method and line number the log entry occurred at.
   */
  def detailed = _detailed

  /**
   * Flag defining whether detailed LogRecords should be created for log records created from this logger.
   *
   * Detailed LogRecords include information on the method and line number the log entry occurred at.
   */
  def detailed_=(detailed: Boolean) = _detailed = detailed

  /**
   * Convenience method to add a file logging handler to this logger.
   *
   * @param name the name to use to derive the filename. Defaults to the logger name.
   * @param directory the directory where logs should be stored.
   * @param level the level of logs to write to the file.
   * @param formatter how logs should be formatted in the output.
   * @return the created and added handler
   */
  def configureFileLogging(name: String = loggerName,
                            directory: File = new File("logs"),
                            level: Level = Level.Info,
                            formatter: Formatter = Formatter.Default) = {
    addHandler(new FileWriter(directory, FileWriter.Daily(name), append = true), level, formatter)
  }

  protected def handleListenerResponse(value: Intercept, state: EventState[LogRecord]) = if (value == Intercept.Stop) {
    state.stopPropagation = true
  }

  protected def responseFor(state: EventState[LogRecord]) = if (state.stopPropagation) {
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
    val boosted = record.boost(multiplier)
    fire(boosted) match {
      case Intercept.Continue => parent match {
        case Some(parentLogger) => parentLogger.log(boosted)
        case None => // Stopped logging
      }
      case Intercept.Stop => // Ignore
    }
  }

  /**
   * Add a handler to this Logger.
   */
  def +=(handler: Handler) = listenable.listeners += handler

  /**
   * Remove a handler from this Logger.
   */
  def -=(handler: Handler) = listenable.listeners -= handler

  /**
   * Creates a handler and adds it to this Logger.
   *
   * @param writer the writer
   * @param level the level to filter
   * @param formatter the formatter
   * @return handler
   */
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

  /**
   * The Root logger. The default top-level Logger for all Loggers unless otherwise specified.
   */
  lazy val Root = {
    val l = apply("root")
    l += DefaultRootHandler
    l
  }
  /**
   * The default root handler associated with the Root Logger by default.
   */
  lazy val DefaultRootHandler = Handler(Formatter.Default, Level.Info, ConsoleWriter)

  /**
   * Creates (if necessary) and retrieves the Logger by the supplied name.
   *
   * @param name the name of the Logger to get.
   * @return Logger
   */
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

  /**
   * Converts a Throwable to a String representation for output in logging.
   */
  final def throwable2String(t: Throwable, primaryCause: Boolean = true): String = {
    // TODO: convert to use tailrec
    val b = new StringBuilder
    if (!primaryCause) {
      b.append("Caused by: ")
    }
    b.append(t.getClass.getName)
    if (t.getLocalizedMessage != null) {
      b.append(": ")
      b.append(t.getLocalizedMessage)
    }
    b.append(System.getProperty("line.separator"))
    writeStackTrace(b, t.getStackTrace)
    if (t.getCause != null) {
      b.append(throwable2String(t.getCause, primaryCause = false))
    }
    b.toString()
  }

  @tailrec
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
      b.append(System.getProperty("line.separator"))
      writeStackTrace(b, elements.tail)
    }
  }
}