package org.powerscala.io

import java.io.{File, IOException}

import scala.annotation.tailrec

object IO {
  @tailrec
  final def stream(reader: Reader,
                   writer: Writer,
                   monitor: Monitor = Monitor.Ignore,
                   buffer: Array[Byte] = new Array[Byte](512),
                   closeOnComplete: Boolean = true): Writer = {
    monitor.open(reader.length)
    val len = reader.read(buffer)
    if (len == -1) {
      writer.flush()
      if (closeOnComplete) {
        writer.close()
        reader.close()
        monitor.closed()
      }
      writer.complete()
      monitor.completed()
      writer
    } else {
      try {
        writer.write(buffer, 0, len)
        monitor.written(len)
      } catch {
        case t: Throwable => {
          monitor.failure(t)
          throw new IOException(s"IO failed to write to writer with length: $len with reader: $reader, writer: $writer.", t)
        }
      }
      stream(reader, writer, monitor, buffer, closeOnComplete)
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
}

trait Reader {
  def length: Option[Long]
  def read(buffer: Array[Byte]): Int
  def close(): Unit
}

trait Writer {
  def write(buffer: Array[Byte], offset: Int, length: Int): Unit
  def flush(): Unit
  def complete(): Unit
  def close(): Unit
}

trait Monitor {
  def open(length: Option[Long]): Unit
  def written(length: Long): Unit
  def failure(t: Throwable): Unit
  def closed(): Unit
  def completed(): Unit
}

object Monitor {
  object Ignore extends Monitor {
    override def open(length: Option[Long]): Unit = {}

    override def written(length: Long): Unit = {}

    override def failure(t: Throwable): Unit = {}

    override def completed(): Unit = {}

    override def closed(): Unit = {}
  }
}