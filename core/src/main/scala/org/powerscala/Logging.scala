package org.powerscala

import org.slf4j.{Logger, LoggerFactory}
import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.{TimeBasedRollingPolicy, RollingFileAppender}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Logging {
  def logger = Logging(this)

  def trace(message: => String) = if (logger.isTraceEnabled) logger.trace(message)
  def debug(message: => String) = if (logger.isDebugEnabled) logger.debug(message)
  def info(message: => String) = if (logger.isInfoEnabled) logger.info(message)
  def warn(message: => String) = if (logger.isWarnEnabled) logger.warn(message)
  def warn(message: => String, throwable: Throwable) = if (logger.isWarnEnabled) logger.warn(message, throwable)
  def error(message: => String) = if (logger.isErrorEnabled) logger.error(message)
  def error(message: => String, throwable: Throwable) = if (logger.isErrorEnabled) logger.error(message, throwable)
}

object Logging {
  val context = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]

  val Standard = "%d{yyyy.MM.dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  val Detailed = "%d{yyyy.MM.dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}.%method.%line%n\t%msg%n"   // TODO: fix so it isn't showing Logging.info instead of the actual method

  reconfigure()

//  def apply(instance: Logging) = LoggerFactory.getLogger(instance.getClass)
  def apply(instance: Logging) = context.getLogger(instance.getClass)

  def reconfigure(pattern: String = Standard, level: Level = Level.INFO, consoleLogging: Boolean = true, fileLogging: Boolean = false) = {
    val configurator = new JoranConfigurator
    context.reset()
    configurator.setContext(context)

    val rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.setAdditive(true)
    rootLogger.setLevel(level)

    if (fileLogging) {
      val encoder = new PatternLayoutEncoder
      encoder.setContext(context)
      encoder.setPattern(pattern)
      encoder.start()

      val fileAppender = new RollingFileAppender[ILoggingEvent]()
      fileAppender.setFile("logs/application.log")
      val rollingPolicy = new TimeBasedRollingPolicy[ILoggingEvent]()
      rollingPolicy.setFileNamePattern("logs/application.%d{yyyy-MM-dd}.log")
      rollingPolicy.setMaxHistory(30)
      rollingPolicy.setContext(context)
      rollingPolicy.setParent(fileAppender)
      fileAppender.setRollingPolicy(rollingPolicy)
      fileAppender.setPrudent(true)
      fileAppender.setName("file")
      fileAppender.setContext(context)
      fileAppender.setEncoder(encoder)
      rollingPolicy.start()
      fileAppender.start()

      rootLogger.addAppender(fileAppender)
    }

    if (consoleLogging) {
      val encoder = new PatternLayoutEncoder
      encoder.setContext(context)
      encoder.setPattern(pattern)
      encoder.start()

      val consoleAppender = new ConsoleAppender[ILoggingEvent]
      consoleAppender.setContext(context)
      consoleAppender.setName("console")
      consoleAppender.setEncoder(encoder)
      consoleAppender.start()

      rootLogger.addAppender(consoleAppender)
    }
  }
}