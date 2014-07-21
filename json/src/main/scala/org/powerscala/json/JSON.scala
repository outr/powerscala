package org.powerscala.json

import org.json4s._
import org.json4s.jackson.JsonMethods
import org.powerscala.Priority
import org.powerscala.event.Listenable
import org.powerscala.event.processor.OptionProcessor
import org.powerscala.json.convert.{EnumEntryConverter, CaseClassSupport, JSONConverter}
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSON extends Listenable {
  val parsers = new OptionProcessor[Any, JValue]("parsers")
  val readers = new OptionProcessor[JValue, Any]("readers")

  JSONClassMap.init()         // JSONClassMap is a quick and efficient mapping of a Class to a parser.
  CaseClassSupport.init()     // CaseClassSupport adds support for case classes.
  EnumEntryConverter.init()   // EnumEntryConverter supports EnumEntries.

  def add[T, J <: JValue](converter: JSONConverter[T, J],
                          typeMatcher: T => Boolean = null,
                          jsonMatcher: J => Boolean = null,
                          priority: Priority = Priority.Normal)(implicit typeManifest: Manifest[T], jsonManifest: Manifest[J]) = {
    val typeClass = typeManifest.runtimeClass
    val jsonClass = jsonManifest.runtimeClass
    parsers.on {
      case value => if (value.getClass.hasType(typeClass)) {
        val v = value.asInstanceOf[T]
        if (typeMatcher == null || typeMatcher(v)) {
          Some(converter.toJSON(v))
        } else {
          None
        }
      } else {
        None
      }
    }
    readers.on {
      case value => if (value.getClass.hasType(jsonClass)) {
        val v = value.asInstanceOf[J]
        if (jsonMatcher == null || jsonMatcher(v)) {
          Some(converter.fromJSON(v))
        } else {
          None
        }
      } else {
        None
      }
    }
  }

  /**
   * Attempts to parse the supplied value into a JValue.
   *
   * @param value the value to parse into a JValue
   * @tparam T the type of the value
   * @return Option[JValue]
   */
  def parse[T](value: T) = parsers.fire(value)

  def read[T](value: JValue) = readers.fire(value)

  def parseAndGet[T](value: T) = parse[T](value).getOrElse(throw new RuntimeException(s"Unable to parse $value as JSON."))

  def readAndGet[T](value: JValue) = read[T](value).getOrElse(throw new RuntimeException(s"Unable to read $value."))

  /**
   * Convenience method to parse the supplied JSON String to a JValue.
   *
   * @param s JSON String
   * @return JValue
   */
  def parseJSON(s: String) = JsonMethods.parse(s)

  /**
   * Convenience method to render the supplied JValue to a JSON String.
   *
   * @param v JValue to render
   * @param pretty flag to render the supplied JSON into a prettily formatted String. Defaults to false.
   * @return JSON String
   */
  def renderJSON(v: JValue, pretty: Boolean = false) = if (pretty) {
    JsonMethods.pretty(v)
  } else {
    JsonMethods.compact(v)
  }
}