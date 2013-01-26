package org.powerscala.log.formatter

import org.powerscala.log.LogRecord
import annotation.tailrec

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
case class FormatterBuilder(items: List[LogRecord => String] = Nil) extends Formatter {
  def string(s: String) = add(FormatterBuilder.Static(s))

  def message = add(FormatterBuilder.Message)

  def date(format: String = "%1$tY.%1$tm.%1$td %1$tT:%1$tL") = add(FormatterBuilder.Date(format))

  def threadName = add(FormatterBuilder.ThreadName)

  def level = add(FormatterBuilder.Level)
  def levelPaddedRight = add(FormatterBuilder.LevelPaddedRight)

  def className = add(FormatterBuilder.ClassName)
  def classNameAbbreviated = add(FormatterBuilder.ClassNameAbbreviated)

  def methodName = add(FormatterBuilder.MethodName)
  def lineNumber = add(FormatterBuilder.LineNumber)

  def newLine = add(FormatterBuilder.NewLine)

  def add(item: LogRecord => String) = copy(items = (item :: items.reverse).reverse)

  def format(record: LogRecord) = {
    val b = new StringBuilder
    process(b, record, items)
    b.toString()
  }

  @tailrec
  private def process(b: StringBuilder, record: LogRecord, l: List[LogRecord => String]): Unit = {
    if (l.nonEmpty) {
      b.append(l.head(record))
      process(b, record, l.tail)
    }
  }
}

object FormatterBuilder {
  def Static(s: String) = (record: LogRecord) => s
  def Date(format: String) = (record: LogRecord) => format.format(record.timestamp)

  val ThreadName = (record: LogRecord) => record.threadName
  val Level = (record: LogRecord) => record.level.name
  val LevelPaddedRight = (record: LogRecord) => record.level.namePaddedRight
  val ClassName = (record: LogRecord) => record.className
  val ClassNameAbbreviated = (record: LogRecord) => record.classNameAbbreviated
  val MethodName = (record: LogRecord) => record.methodName
  val LineNumber = (record: LogRecord) => record.lineNumber.toString
  val Message = (record: LogRecord) => record.message
  val NewLine = (record: LogRecord) => System.lineSeparator()
}