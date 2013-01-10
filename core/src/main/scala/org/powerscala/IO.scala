package org.powerscala

import java.io._
import annotation.tailrec

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

  def copy(read: File, write: File) = {
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

  def copy(read: String, write: File) = {
    val output = new FileOutputStream(write)
    try {
      output.write(read.getBytes)
    } finally {
      output.flush()
      output.close()
    }
  }

  def copy(read: File) = {
    val input = new FileInputStream(read)
    val output = new ByteArrayOutputStream(read.length().toInt)
    stream(input, output)
    val bytes = output.toByteArray
    new String(bytes)
  }
}
