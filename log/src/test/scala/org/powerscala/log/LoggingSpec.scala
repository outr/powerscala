package org.powerscala.log

import org.powerscala.log.formatter.Formatter
import org.powerscala.log.writer.Writer
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable.ListBuffer

/**
 * @author Matt Hicks <matt@outr.com>
 */
class LoggingSpec extends WordSpec with Matchers with Logging {
  logger.parent = None      // Remove parent reference to Root logger
  val handler = logger.addHandler(TestingWriter, Level.Debug)

  "Logging" should {
    "have no logged entries yet" in {
      TestingWriter.records.length should be(0)
    }
    "log a single entry after info log" in {
      info("Info Log")
      TestingWriter.records.length should be(1)
    }
    "log a second entry after debug log" in {
      debug("Debug Log")
      TestingWriter.records.length should be(2)
    }
    "ignore the third entry after reconfiguring without debug logging" in {
      logger -= handler
      logger.addHandler(TestingWriter, Level.Info)
      debug("Debug Log 2")
      TestingWriter.records.length should be(2)
    }
    "boost the this logging instance" in {
      logger.multiplier = 2.0
      debug("Debug Log 3")
      TestingWriter.records.length should be(3)
    }
    "not increment when logging to the root logger" in {
      Logger.Root.error("Error Log 1")
      TestingWriter.records.length should be(3)
    }
    "write a detailed log message" in {
      TestingWriter.clear()
      LoggingTestObject.testLogger()
      TestingWriter.records.length should be(1)
      TestingWriter.records.head.methodName should be(Some("testLogger"))
      TestingWriter.records.head.lineNumber should be(Some(59))
    }
  }
}

object LoggingTestObject extends Logging {
  logger.parent = None      // Remove parent reference to Root logger
  logger.detailed = true    // Log details (method and line number)
  logger.addHandler(TestingWriter, Level.Debug)

  def testLogger() = {
    info("This is a test!")
  }
}

object TestingWriter extends Writer {
  val records = ListBuffer.empty[LogRecord]

  def write(record: LogRecord, formatter: Formatter) = records += record

  def clear() = records.clear()
}