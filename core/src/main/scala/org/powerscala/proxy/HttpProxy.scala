package org.powerscala.proxy

import java.io.{ByteArrayOutputStream, ByteArrayInputStream, InputStream}
import java.net.{InetAddress, ServerSocket}

import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
class HttpProxy(host: String = null, port: Int = 8080, backlog: Int = 50) {
  private var server: ServerSocket = _

  def start() = {
    server = new ServerSocket(port, backlog, if (host != null) InetAddress.getByName(host) else null)
//    new Thread() {
//      override def run() = {
        val socket = server.accept()
        val stream = new DelayedCrossStream(socket.getInputStream)
//      }
//    }
  }
}

object HttpProxy {
  def main(args: Array[String]): Unit = {
    val proxy = new HttpProxy()
    proxy.start()
  }
}

class DelayedCrossStream(input: InputStream) {
  private val buffer = new ByteArrayOutputStream
  private val builder = new StringBuilder

  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")
  println(s"Line: ${bufferLine()}")

  @tailrec
  private def bufferLine(): String = {
    val b = input.read()
    val c = b.toChar
    buffer.write(b)
    if (c == '\n') {
      val s = builder.toString()
      builder.clear()
      s
    } else {
      if (c != '\r') {
        builder.append(c)
      }
      bufferLine()
    }
  }
}
