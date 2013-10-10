package org.powerscala.log.slf4j.impl

import org.slf4j.helpers.MarkerIgnoringBase
import org.slf4j.Logger
import org.powerscala.log.{Level, LoggingCore}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PSLoggerAdapter(className: String) extends MarkerIgnoringBase with Logger with LoggingCore {
  override protected def loggingClassName = className

  def error(msg: String, t: Throwable) = {
    log(Level.Error, msg)
    log(Level.Error, t)
  }

  def error(format: String, arguments: AnyRef*) = log(Level.Error, format.format(arguments: _*))

  def error(format: String, arg1: scala.Any, arg2: scala.Any) = log(Level.Error, format.format(arg1, arg2))

  def error(format: String, arg: scala.Any) = log(Level.Error, format.format(arg))

  def error(msg: String) = log(Level.Error, msg)

  def warn(msg: String, t: Throwable) = {
    log(Level.Warn, msg)
    log(Level.Warn, t)
  }

  def warn(format: String, arg1: scala.Any, arg2: scala.Any) = log(Level.Warn, format.format(arg1, arg2))

  def warn(format: String, arguments: AnyRef*) = log(Level.Warn, format.format(arguments: _*))

  def warn(format: String, arg: scala.Any) = log(Level.Warn, format.format(arg))

  def warn(msg: String) = log(Level.Warn, msg)

  def info(msg: String, t: Throwable) = {
    log(Level.Info, msg)
    log(Level.Info, t)
  }

  def info(format: String, arguments: AnyRef*) = log(Level.Info, format.format(arguments: _*))

  def info(format: String, arg1: scala.Any, arg2: scala.Any) = log(Level.Info, format.format(arg1, arg2))

  def info(format: String, arg: scala.Any) = log(Level.Info, format.format(arg))

  def info(msg: String) = log(Level.Info, msg)

  def debug(msg: String, t: Throwable) = {
    log(Level.Debug, msg)
    log(Level.Debug, t)
  }

  def debug(format: String, arguments: AnyRef*) = log(Level.Debug, format.format(arguments: _*))

  def debug(format: String, arg1: scala.Any, arg2: scala.Any) = log(Level.Debug, format.format(arg1, arg2))

  def debug(format: String, arg: scala.Any) = log(Level.Debug, format.format(arg))

  def debug(msg: String) = log(Level.Debug, msg)

  def trace(msg: String, t: Throwable) = {
    log(Level.Trace, msg)
    log(Level.Trace, t)
  }

  def trace(format: String, arguments: AnyRef*) = log(Level.Trace, format.format(arguments: _*))

  def trace(format: String, arg1: scala.Any, arg2: scala.Any) = log(Level.Trace, format.format(arg1, arg2))

  def trace(format: String, arg: scala.Any) = log(Level.Trace, format.format(arg))

  def trace(msg: String) = log(Level.Trace, msg)

  def isTraceEnabled = logger.isLevelEnabled(Level.Trace)

  def isDebugEnabled = logger.isLevelEnabled(Level.Debug)

  def isInfoEnabled = logger.isLevelEnabled(Level.Info)

  def isWarnEnabled = logger.isLevelEnabled(Level.Warn)

  def isErrorEnabled = logger.isLevelEnabled(Level.Error)
}
