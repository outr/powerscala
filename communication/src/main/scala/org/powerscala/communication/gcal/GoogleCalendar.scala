package org.powerscala.communication.gcal

import java.net.{URL, URLEncoder}
import util.parsing.json.JSON
import org.powerscala.IO
import java.util.{TimeZone, Calendar}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class GoogleCalendar(email: String, maxResults: Int = 8, token: String, timeMin: Long = System.currentTimeMillis()) {
  val minimum = "%1$tY-%1$tm-%1$tdT00:00:00-06:00".format(timeMin)
  val url = new URL("https://www.googleapis.com/calendar/v3/calendars/%s/events?maxResults=%s&orderBy=startTime&singleEvents=true&timeMin=%s&key=%s".format(URLEncoder.encode(email, "UTF-8"), maxResults, URLEncoder.encode(minimum, "UTF-8"), token))
  val content = IO.copy(url)
  val map = JSON.parseFull(content) match {
    case Some(parsed) => parsed.asInstanceOf[Map[String, Any]]
    case None => throw new RuntimeException("Unable to parse: %s".format(content))
  }
  val busyOnly = map("accessRole") == "freeBusyReader"
  private val _items = map.getOrElse("items", Nil).asInstanceOf[List[Map[String, Any]]]
  val items = _items.map(m => CalendarItem(m))
}

case class CalendarItem(kind: String, id: String, status: String, summary: String, updated: Long, start: Long, end: Long, iCalUID: String) {
  override def toString = {
    "CalendarItem(kind = \"%s\", id = \"%s\", status = \"%s\", summary = \"%s\", updated = \"%tc\", start = \"%tc\", end = \"%tc\", iCalUID = \"%s\")".format(kind, id, status, summary, updated, start, end, iCalUID)
  }
}

object CalendarItem {
  val ParserRegex1 = """(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})[.](\d{3})Z""".r
  val ParserRegex2 = """(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})-(\d{2}):(\d{2})""".r
  val ParserRegex3 = """(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})[+](\d{2}):(\d{2})""".r
  val ParserRegex4 = """(\d{4})-(\d{2})-(\d{2})""".r

  def apply(map: Map[String, Any]) = {
    val kind = map("kind").asInstanceOf[String]
    val id = map("id").asInstanceOf[String]
    val status = map("status").asInstanceOf[String]
    val summary = map.getOrElse("summary", "busy").asInstanceOf[String]
    val updated = parseDate(map("updated").asInstanceOf[String])
    val start = extractDate("start", map)
    val end = extractDate("start", map)
    val iCalUID = map("iCalUID").asInstanceOf[String]
    new CalendarItem(kind, id, status, summary, updated, start, end, iCalUID)
  }

  private def extractDate(key: String, map: Map[String, Any]) = {
    val container = map(key).asInstanceOf[Map[String, String]]
    if (container.contains("dateTime")) {
      parseDate(container("dateTime"))
    } else {
      parseDate(container("date"))
    }
  }

  def parseDate(date: String) = date match {
    case ParserRegex1(year, month, day, hour, minute, second, millisecond) => {
      val calendar = Calendar.getInstance(TimeZone.getTimeZone("Zulu"))
      calendar.set(year.toInt, month.toInt - 1, day.toInt, hour.toInt, minute.toInt, second.toInt)
      calendar.getTimeInMillis
    }
    case ParserRegex2(year, month, day, hour, minute, second, tzHour, tzMinute) => {
      val offset = -((tzHour.toInt * 1000 * 60 * 60) + (tzMinute.toInt * 1000 * 60))
      val timeZone = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offset).head)
      val calendar = Calendar.getInstance(timeZone)
      calendar.set(year.toInt, month.toInt - 1, day.toInt, hour.toInt, minute.toInt, second.toInt)
      calendar.getTimeInMillis
    }
    case ParserRegex3(year, month, day, hour, minute, second, tzHour, tzMinute) => {
      val offset = (tzHour.toInt * 1000 * 60 * 60) + (tzMinute.toInt * 1000 * 60)
      val timeZone = TimeZone.getTimeZone(TimeZone.getAvailableIDs(offset).head)
      val calendar = Calendar.getInstance(timeZone)
      calendar.set(year.toInt, month.toInt - 1, day.toInt, hour.toInt, minute.toInt, second.toInt)
      calendar.getTimeInMillis
    }
    case ParserRegex4(year, month, day) => {
      val calendar = Calendar.getInstance()
      calendar.set(year.toInt, month.toInt - 1, day.toInt, 0, 0, 0)
      calendar.getTimeInMillis
    }
  }
}