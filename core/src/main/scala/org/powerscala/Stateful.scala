package org.powerscala

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

/**
 * Stateful provides the functionality of a Map, but with the ability for values to timeout if they don't checkIn
 * periodically.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class Stateful[Key, Value](timeout: Long) {
  private val map = new ConcurrentHashMap[Key, Entry]

  def get(key: Key): Option[Value] = map.get(key) match {
    case null => None
    case entry => if (validate(key, entry)) {
      Some(entry.value)
    } else {
      None
    }
  }

  def set(key: Key, value: Value) = map.put(key, Entry(value))

  def checkIn(key: Key) = map.get(key) match {
    case null => false // Not found
    case entry => {
      entry.lastUpdated = System.currentTimeMillis()
      true
    }
  }

  def validate(key: Key, entry: Entry) = if (entry.lastUpdated < System.currentTimeMillis() - timeout) {
    map.remove(key)
    false
  } else {
    true
  }

  def update() = map.foreach {
    case (key, entry) => validate(key, entry)
  }

  case class Entry(value: Value, var lastUpdated: Long = System.currentTimeMillis())
}