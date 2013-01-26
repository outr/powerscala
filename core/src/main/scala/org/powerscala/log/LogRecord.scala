package org.powerscala.log

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class LogRecord(val level: Level, _message: => String, val className: String, val timestamp: Long = System.currentTimeMillis(), asynchronous: Boolean = false) {
  lazy val message = _message
  lazy val threadId = Thread.currentThread().getId
  lazy val threadName = Thread.currentThread().getName
  lazy val classNameAbbreviated = {
    val b = new StringBuilder
    abbreviate(className.split("[.]").toList, b)
    b.toString()
  }
  lazy val stackTrace = Thread.currentThread().getStackTrace
  lazy val (methodName, lineNumber) = parseOrigin()

  if (asynchronous) {     // We must load all the thread and stacktrace info immediately
    threadId
    threadName
    stackTrace      // TODO: only load stackTrace when a handler will use it?
  }

  private def parseOrigin() = {
    val current = if (asynchronous) {
      stackTrace(7)
    } else {
      stackTrace(16)
    }
    (current.getMethodName, current.getLineNumber)
  }

  private def abbreviate(values: List[String], b: StringBuilder): Unit = {
    if (values.nonEmpty) {
      if (values.tail.isEmpty) {
        b.append(values.head)
      } else {
        b.append(values.head.charAt(0))
        b.append('.')
        abbreviate(values.tail, b)
      }
    }
  }
}