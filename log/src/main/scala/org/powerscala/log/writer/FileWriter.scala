package org.powerscala.log.writer

import org.powerscala.concurrent.AtomicInt
import org.powerscala.log.LogRecord
import org.powerscala.log.formatter.Formatter
import java.io.{BufferedOutputStream, FileOutputStream, File}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class FileWriter(val directory: File,
                 val filenameGenerator: () => String,
                 val append: Boolean = true,
                 val autoFlush: Boolean = true) extends Writer {
  private var currentFilename: String = _
  private var handle: FileHandle = _

  protected def checkOutput() = synchronized {
    val filename = filenameGenerator()
    if (handle == null) {
      currentFilename = filename
      handle = FileHandle(new File(directory, filename), append)
    } else if (currentFilename != filename) {
      FileHandle.release(handle)
      handle = FileHandle(new File(directory, filename), append)
      currentFilename = filename
    }
  }

  def write(record: LogRecord, formatter: Formatter) = {
    checkOutput()
    handle.write(formatter.format(record), autoFlush)
  }

  def close() = if (handle != null) {
    FileHandle.release(handle)
  }
}

object FileWriter {
  def DatePattern(pattern: String) = () => pattern.format(System.currentTimeMillis())

  def Daily(name: String = "application") = DatePattern(name + ".%1$tY-%1$tm-%1$td.log")
}

class FileHandle(val file: File, append: Boolean) {
  val references = new AtomicInt(0)

  // Make sure the directories exist
  file.getParentFile.mkdirs()

  private val output = new BufferedOutputStream(new FileOutputStream(file, append))

  def write(s: String, autoFlush: Boolean) = {
    output.write(s.getBytes)
    if (autoFlush) {
      output.flush()
    }
  }

  def close() = {
    output.flush()
    output.close()
  }
}

object FileHandle {
  private var map = Map.empty[File, FileHandle]

  def apply(file: File, append: Boolean) = synchronized {
    val h = map.get(file) match {
      case Some(handle) => handle
      case None => {
        val handle = new FileHandle(file, append)
        map += file -> handle
        handle
      }
    }
    h.references += 1
    h
  }

  def release(handle: FileHandle) = synchronized {
    handle.references -= 1
    if (handle.references() == 0) {
      map -= handle.file
      handle.close()
    }
  }
}