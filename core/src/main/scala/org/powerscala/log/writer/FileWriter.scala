package org.powerscala.log.writer

import org.powerscala.log.LogRecord
import org.powerscala.log.formatter.Formatter
import java.io.{BufferedOutputStream, FileOutputStream, File}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class FileWriter(val directory: File, val filenameGenerator: () => String, val append: Boolean = true) extends Writer {
  private var currentFilename: String = _
  private var output: BufferedOutputStream = _

  protected def checkOutput(out: BufferedOutputStream) = {
    val filename = filenameGenerator()
    if (out == null) {
      currentFilename = filename
      new BufferedOutputStream(new FileOutputStream(new File(directory, filename), append))
    } else if (currentFilename != filename) {
      out.flush()
      out.close()
      currentFilename = filename
      new BufferedOutputStream(new FileOutputStream(new File(directory, filename), append))
    } else {
      out
    }
  }

  def write(record: LogRecord, formatter: Formatter) = {
    output = checkOutput(output)
    output.write(formatter.format(record).getBytes)
    output.flush()
  }

  def close() = {
    output.flush()
    output.close()
  }
}

object FileWriter {
  def DatePattern(pattern: String) = () => pattern.format(System.currentTimeMillis())
}
