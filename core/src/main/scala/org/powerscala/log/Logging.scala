package org.powerscala.log

import akka.actor.{Actor, Props, ActorSystem}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Logging {
  protected def asynchronousLogging = true
  protected lazy val className = getClass.getName

  val logger = new {
    def apply() = Logging(Logging.this)
    def isLevelEnabled(level: Level) = apply().isLevelEnabled(level)
    def parent = apply().parent
  }

  def trace(message: => String) = log(Level.Trace, message)
  def debug(message: => String) = log(Level.Debug, message)
  def info(message: => String) = log(Level.Info, message)
  def warn(message: => String) = log(Level.Warn, message)
  def error(message: => String) = log(Level.Error, message)

  def log(level: Level, message: => String) = if (logger.isLevelEnabled(level)) {
    val record = new LogRecord(level, message, className, asynchronous = asynchronousLogging)
    if (asynchronousLogging) {
      Logging.actor ! (() => logger().log(record))
    } else {
      logger().log(record)
    }
  }
}

object Logging {
  System.setProperty("akka.daemonic", "on")
  private val system = ActorSystem("LoggingActorSystem")
  private val actor = system.actorOf(Props[AsynchronousLoggingActor], name = "asynchronousLoggingActor")

  def apply(logging: Logging): Logger = Logger(logging.getClass.getName)
}

class AsynchronousLoggingActor extends Actor {
  context.dispatcher.inhabitants

  def receive = {
    case f: Function0[_] => f()
  }
}

object Test extends Logging {
  def main(args: Array[String]): Unit = {
    info("Hello World!")
    trace("A trace!")
    error("An error!")
    (0 until 100).foreach {
      case i => info("Number %s".format(i))
    }
    test()
    Thread.sleep(1000)
  }

  def test() = {
    warn("Another?")
  }
}