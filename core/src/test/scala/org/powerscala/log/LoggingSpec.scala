package org.powerscala.log

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import org.powerscala.log.writer.Writer
import org.powerscala.log.formatter.Formatter
import scala.collection.mutable.ListBuffer

/**
 * @author Matt Hicks <matt@outr.com>
 */
class LoggingSpec extends WordSpec with ShouldMatchers with Logging {
  logger.configure {
    case l => l.withHandler(writer = TestingWriter).withLevel(Level.Debug)
  }

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
      logger.configure {
        case l => l.withLevel(Level.Info)
      }
      debug("Debug Log 2")
      TestingWriter.records.length should be(2)
    }
  }
}

object TestingWriter extends Writer {
  val records = ListBuffer.empty[LogRecord]

  def write(record: LogRecord, formatter: Formatter) = records += record

  def clear() = records.clear()
}