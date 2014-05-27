package org.powerscala.log

import akka.actor.{Actor, Props, ActorSystem}
import java.util.concurrent.atomic.AtomicInteger
import java.io.PrintStream

/**
 * Logging trait can be mixed in to provide class-level logging.
 *
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Logging extends LoggingCore {
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
  val systemOut = System.out
  val systemErr = System.err
  private[log] val asynchronous = new AtomicInteger(0)

  /**
   * Returns the number of asynchronous logging requests are currently queued
   */
  def queued = asynchronous.get()

  lazy val root = new InnerLogging("root")

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
  private[log] lazy val system = ActorSystem("LoggingActorSystem")
  private[log] lazy val actor = system.actorOf(Props[AsynchronousLoggingActor], name = "asynchronousLoggingActor")

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
//  context.dispatcher.inhabitants

  def receive = {
    case f: Function0[_] => f()
  }
}