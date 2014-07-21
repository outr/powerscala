package org.powerscala.json

import org.json4s._
import org.json4s.jackson.JsonMethods
import org.powerscala.event.Listenable
import org.powerscala.event.processor.OptionProcessor
import org.powerscala.json.convert.JSONConverter
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSON extends Listenable {
  val parsers = new OptionProcessor[Any, JValue]("parsers")
  val readers = new OptionProcessor[JValue, Any]("readers")

  parsers += JSONClassMap.parserListener         // JSONClassMap is a quick and efficient mapping of a Class to a parser.
  readers += JSONClassMap.readerListener         // JSONClassMap is a quick and efficient mapping of a Class to a reader.

  def +=[T, J <: JValue](converter: JSONConverter[T, J], matcher: T => Boolean = null)(implicit manifest: Manifest[T]) = {
    val clazz = manifest.runtimeClass
    parsers.on {
      case value => if (value.getClass.hasType(clazz)) {
        val v = value.asInstanceOf[T]
        if (matcher == null || matcher(v)) {
          Some(converter.toJSON(v))
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