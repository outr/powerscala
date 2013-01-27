package org.powerscala.log.formatter

import org.powerscala.log.LogRecord
import annotation.tailrec
import util.matching.Regex
import collection.mutable.ListBuffer

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
  type FormatEntry = LogRecord => String

  private var map = Map.empty[String, String => FormatEntry]
  def add(name: String, f: String => FormatEntry) = synchronized {
    map += name -> f
  }

  add("string", (s: String) => Static(s))
  add("date", (s: String) => if (s == null) Date() else Date(s))
  add("threadName", (s: String) => ThreadName)
  add("level", (s: String) => Level)
  add("levelPaddedRight", (s: String) => LevelPaddedRight)
  add("className", (s: String) => ClassName)
  add("classNameAbbreviated", (s: String) => ClassNameAbbreviated)
  add("methodName", (s: String) => MethodName)
  add("lineNumber", (s: String) => LineNumber)
  add("message", (s: String) => Message)
  add("newLine", (s: String) => NewLine)

  def Static(s: String) = (record: LogRecord) => s
  def Date(format: String = "%1$tY.%1$tm.%1$td %1$tT:%1$tL") = (record: LogRecord) => format.format(record.timestamp)

  val ThreadName = (record: LogRecord) => record.threadName
  val Level = (record: LogRecord) => record.level.name
  val LevelPaddedRight = (record: LogRecord) => record.level.namePaddedRight
  val ClassName = (record: LogRecord) => record.className
  val ClassNameAbbreviated = (record: LogRecord) => record.classNameAbbreviated
  val MethodName = (record: LogRecord) => record.methodName
  val LineNumber = (record: LogRecord) => record.lineNumber.toString
  val Message = (record: LogRecord) => record.message
  val NewLine = (record: LogRecord) => System.lineSeparator()

  private val regex = """\$\{(.*?)\}""".r

  def parse(s: String) = {
    val results = regex.findAllIn(s)
    FormatterBuilder(processRecursive(results))
  }

  private def processRecursive(iterator: Regex.MatchIterator,
                               list: ListBuffer[FormatEntry] = ListBuffer.empty[FormatEntry],
                               previousEnd: Int = 0): List[FormatEntry] = {
    if (!iterator.hasNext) {
      val after = iterator.source.subSequence(previousEnd, iterator.source.length())
      if (after.length() > 0) {
        list += FormatterBuilder.Static(after.toString)
      }
      list.toList
    } else {
      iterator.next()
      if (iterator.start > previousEnd) {
        val before = iterator.source.subSequence(previousEnd, iterator.start)
        list += FormatterBuilder.Static(before.toString)
      }
      val block = iterator.group(1)
      val separator = block.indexOf(':')
      val (name, value) = if (separator != -1) {
        (block.substring(0, separator), block.substring(separator + 1))
      } else {
        (block, null)
      }
      list += parseBlock(name, value)
      processRecursive(iterator, list, iterator.end)
    }
  }

  protected def parseBlock(name: String, value: String): FormatEntry = {
    map(name)(value)
  }
}