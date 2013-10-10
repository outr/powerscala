package org.powerscala.log

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait LoggingCore {
  import language.reflectiveCalls

  /**
   * Determines whether logging should be asynchronous.
   *
   * Defaults to false
   */
  protected def asynchronousLogging = false
  protected def loggingClassName = getClass.getName

  val logger = new InnerLogging(loggingClassName)

  def log(level: Level, message: => Any): Unit = if (logger.isLevelEnabled(level)) {
    val messageString = message match {
      case null => null
      case s: String => s
      case t: Throwable => Logging.throwable2String(t)
      case _ => message.toString
    }
    val record = new LogRecord(level = level, _message = messageString, className = getClass.getName, asynchronous = asynchronousLogging)
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
