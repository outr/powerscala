package org.powerscala.util

import java.net.ServerSocket

import scala.annotation.tailrec

object NetUtil {
  @tailrec
  final def nextTCPPort(from: Int = 1024, to: Int = Int.MaxValue): Option[Int] = {
    if (isTCPPortFree(from)) {
      Some(from)
    } else if (from == to) {
      None
    } else {
      nextTCPPort(from + 1, to)
    }
  }

  def availableTCPPort(): Int = {
    val ss = new ServerSocket(0)
    try {
      ss.getLocalPort
    } finally {
      ss.close()
    }
  }

  def isTCPPortFree(port: Int): Boolean = try {
    new ServerSocket(port).close()
    true
  } catch {
    case t: Throwable => false
  }
}