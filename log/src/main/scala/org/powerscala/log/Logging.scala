package org.powerscala.log

/**
 * Logging provides an easily mixed-in trait to provide logging to a class.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait Logging {
  /**
   * Override this to change the name of the underlying logger.
   *
   * Defaults to class name with package
   */
  protected def loggerName = getClass.getName

  /**
   * Logger to log to.
   */
  def logger = Logger(loggerName)

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

  def log(level: Level, message: => Any) = logger.log(level, message)
}
