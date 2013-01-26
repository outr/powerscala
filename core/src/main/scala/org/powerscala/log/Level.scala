package org.powerscala.log

import math._

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
case class Level(name: String, value: Int) {
  Level += this   // Add it to the known list of levels

  def namePaddedRight = name.padTo(Level.maxLength, " ").mkString
}

object Level {
  private var _levels = List.empty[Level]
  private var _maxLength = 0

  def levels = _levels
  def apply(name: String) = levels.find(l => l.name.equalsIgnoreCase(name))
  def maxLength = _maxLength

  val Trace = Level("TRACE", 5000)
  val Debug = Level("DEBUG", 10000)
  val Info = Level("INFO", 20000)
  val Warn = Level("WARN", 30000)
  val Error = Level("ERROR", 40000)

  private def +=(level: Level) = synchronized {
    _levels = (level :: _levels).sortBy(l => l.value)
    _maxLength = max(_maxLength, level.name.length)
  }
}