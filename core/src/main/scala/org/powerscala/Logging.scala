package org.powerscala

import org.slf4j.{Logger, LoggerFactory}
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Logging {
  lazy val logger = Logging(this)

  def trace(message: => String) = if (logger.isTraceEnabled) logger.trace(message)
  def debug(message: => String) = if (logger.isDebugEnabled) logger.debug(message)
  def info(message: => String) = if (logger.isInfoEnabled) logger.info(message)
  def warn(message: => String) = if (logger.isWarnEnabled) logger.warn(message)
  def warn(message: => String, throwable: Throwable) = if (logger.isWarnEnabled) logger.warn(message, throwable)
  def error(message: => String) = if (logger.isErrorEnabled) logger.error(message)
  def error(message: => String, throwable: Throwable) = if (logger.isErrorEnabled) logger.error(message, throwable)
}

object Logging {
  val Standard = "%d{yyyy.MM.dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  val Detailed = "%d{yyyy.MM.dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}.%method.%line%n\t%msg%n"

  reconfigure()

  def apply(instance: Logging) = LoggerFactory.getLogger(instance.getClass)

  def reconfigure(pattern: String = Standard) = {
    val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val configurator = new JoranConfigurator
    configurator.setContext(context)
    context.reset()
    val appender = new ConsoleAppender[ILoggingEvent]
    appender.setContext(context)
    appender.setName("console")
    val encoder = new PatternLayoutEncoder
    encoder.setContext(context)
    encoder.setPattern(pattern)
    encoder.start()
    appender.setEncoder(encoder)
    appender.start()
    val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.addAppender(appender)
  }
}