package org.powerscala.log

/**
 * LogRecord represents a single logging event.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class LogRecord(level: Level,
                     message: () => Any,
                     name: String,
                     multiplier: Double = 1.0,
                     methodName: Option[String] = None,
                     lineNumber: Option[Int] = None,
                     threadId: Long = Thread.currentThread().getId,
                     threadName: String = Thread.currentThread().getName,
                     timestamp: Long = System.currentTimeMillis()) {
  def value = level.value * multiplier

  def boost(multiplier: Double) = if (multiplier != 1.0) copy(multiplier = this.multiplier * multiplier) else this
}

object LogRecord {
  def trace(level: Level, message: () => Any, name: String, multiplier: Double = 1.0, timestamp: Long = System.currentTimeMillis()) = {
    val stackTrace = Thread.currentThread().getStackTrace
    val element = stackTrace.find(ste => isValid(ste, level)).getOrElse(throw new NullPointerException(s"Unable to find $name in stack trace (${stackTrace.map(ste => ste.getClassName).mkString(", ")}})!"))
//    stackTrace.foreach(ste => println(s"Element: ${ste.getClassName} - ${ste.getMethodName}"))
//    val element = stackTrace.find(ste => ste.getClassName == className).getOrElse(throw new NullPointerException(s"Unable to find $className in stack trace (${stackTrace.map(ste => ste.getClassName).mkString(", ")}})!"))
    val methodName = Some(element.getMethodName)
    val lineNumber = Some(element.getLineNumber)
    LogRecord(level, message, name, multiplier, methodName, lineNumber, timestamp = timestamp)
  }

  private def isValid(ste: StackTraceElement, level: Level) = if (ste.getClassName == "java.lang.Thread") {
    false
  } else if (ste.getClassName == "org.powerscala.log.LogRecord$") {
    false
  } else if (ste.getMethodName == "log") {
    false
  } else if (ste.getMethodName == level.name.toLowerCase) {
    false
  } else {
    true
  }
}