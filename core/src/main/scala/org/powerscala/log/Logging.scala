package org.powerscala.log

import akka.actor.{Actor, Props, ActorSystem}
import java.util.concurrent.atomic.AtomicInteger

/**
 * Logging trait can be mixed in to provide class-level logging.
 *
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Logging {
  /**
   * Determines whether logging should be asynchronous.
   *
   * Defaults to true
   */
  protected def asynchronousLogging = true

  val logger = new InnerLogging(getClass.getName)

  def trace(message: => String): Unit = log(Level.Trace, message)
  def debug(message: => String): Unit = log(Level.Debug, message)
  def info(message: => String): Unit = log(Level.Info, message)
  def warn(message: => String): Unit = log(Level.Warn, message)
  def warn(t: Throwable): Unit = log(Level.Warn, Logging.throwable2String(t))
  def warn(message: => String, t: Throwable): Unit = {
    log(Level.Warn, message)
    warn(t)
  }
  def error(message: => String): Unit = log(Level.Error, message)
  def error(t: Throwable): Unit = log(Level.Error, Logging.throwable2String(t))
  def error(message: => String, t: Throwable): Unit = {
    log(Level.Error, message)
    error(t)
  }

  def log(level: Level, message: => String) = if (logger.isLevelEnabled(level)) {
    val record = new LogRecord(level = level, _message = message, className = getClass.getName, asynchronous = asynchronousLogging)
    if (asynchronousLogging) {
      Logging.asynchronous.incrementAndGet()      // Keep track of unsaved logs
      val f = () => {
        logger().log(record)
        Logging.asynchronous.decrementAndGet()
      }
      Logging.actor ! f
    } else {
      logger().log(record)
    }
  }
}

class InnerLogging(className: String) {
  /**
   * Logger for this instance. Defaults to root logger if no explicit logger is configured.
   */
  def apply() = Logging(className)

  /**
   * Determines if any logging exists for the supplied level.
   */
  def isLevelEnabled(level: Level) = apply().isLevelEnabled(level)

  /**
   * Configures this class-level Logger. If no instance currently exists a new one will be created.
   */
  def configure(f: Logger => Logger) = Logger.configure(className)(f)
}

object Logging {
  private val asynchronous = new AtomicInteger(0)

  /**
   * Returns the number of asynchronous logging requests are currently queued
   */
  def queued = asynchronous.get()

  lazy val root = new InnerLogging("root")

  /**
   * Waits for the queue to become empty or the timeout to elapse.
   *
   * @param timeout the amount of time to wait for the queue to empty (defaults to 5.0 - five seconds)
   * @param precision the frequency at which the queue is checked (defaults to 0.01 - every 10 milliseconds)
   * @return true if queue is empty upon exit
   */
  def await(timeout: Double = 5.0, precision: Double = 0.01) = {
    val start = System.currentTimeMillis()
    val timeoutLong = start + math.round(timeout * 1000.0)
    val precisionLong = math.round(precision * 1000.0)
    while (queued != 0 || System.currentTimeMillis() < timeoutLong) {
      Thread.sleep(precisionLong)
    }
    queued == 0
  }

  System.setProperty("akka.daemonic", "on")
  private val system = ActorSystem("LoggingActorSystem")
  private val actor = system.actorOf(Props[AsynchronousLoggingActor], name = "asynchronousLoggingActor")

  def apply(logging: Logging): Logger = Logger(logging.getClass.getName)

  def apply(name: String) = Logger(name)

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

class AsynchronousLoggingActor extends Actor {
  context.dispatcher.inhabitants

  def receive = {
    case f: Function0[_] => f()
  }
}