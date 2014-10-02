package org.powerscala

import java.io._
import annotation.tailrec
import java.net.URL
import io.Source

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
object IO {
  @tailrec
  final def stream(input: InputStream,
                   output: OutputStream,
                   buf: Array[Byte] = new Array[Byte](512),
                   closeOnComplete: Boolean = true,
                   written: Long = 0L): Long = {
    val len = input.read(buf)
    if (len == -1) {
      output.flush()
      if (closeOnComplete) {
        output.close()
        input.close()
      }
      written
    } else {
      output.write(buf, 0, len)
      stream(input, output, buf, closeOnComplete, written + len)
    }
  }

  def stream(input: InputStream, output: File): Long = stream(input, new FileOutputStream(output))

  def copy(read: File, write: File): File = {
    val parent = write.getParentFile
    if (parent != null) {
      parent.mkdirs()
    }
    if (read.isDirectory) {   // Copy a directory
      write.mkdirs()
      read.listFiles().foreach {
        case f => copy(f, new File(write, f.getName))
      }
    } else {
      val input = new FileInputStream(read)
      val output = new FileOutputStream(write)
      try {
        stream(input, output)
      } catch {
        case t: Throwable => {
          output.flush()
          output.close()
          input.close()
          throw t
        }
      }
    }
    write
  }

  def copy(read: String, write: File) = {
    val output = new FileOutputStream(write)
    try {
      output.write(read.getBytes)
    } finally {
      output.flush()
      output.close()
    }
    write
  }

  def copy(read: File) = {
    val input = new FileInputStream(read)
    val output = new ByteArrayOutputStream(read.length().toInt)
    stream(input, output, closeOnComplete = true)
    val bytes = output.toByteArray
    new String(bytes)
  }

  def copy(read: URL) = {
    val source = Source.fromURL(read)
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  def copy(input: InputStream) = {
    val source = Source.fromInputStream(input)
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  def delete(file: File) = {
    if (file.isDirectory) {
      deleteFiles(file.listFiles().toList)
    }
    file.delete()
  }

  @tailrec
  final def deleteFiles(files: List[File]): Unit = {
    if (files.nonEmpty) {
      val f = files.head
      delete(f)
      deleteFiles(files.tail)
    }
  }

  def lastModified(url: URL) = {
    val connection = url.openConnection()
    connection.getLastModified
  }
}
