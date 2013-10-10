package org.powerscala.log.slf4j.impl

import org.slf4j.{Logger, ILoggerFactory}
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Matt Hicks <matt@outr.com>
 */
object PSLoggerFactory extends ILoggerFactory {
  private val loggerMap = new ConcurrentHashMap[String, Logger]()

  def getLogger(name: String) = synchronized {
    val n = if (name.equalsIgnoreCase(Logger.ROOT_LOGGER_NAME)) {
      "root"
    } else {
      name
    }
    loggerMap.get(n) match {
      case null => {    // Create the logger
        val adapter = new PSLoggerAdapter(n)
        loggerMap.put(n, adapter)
        adapter
      }
      case adapter => adapter
    }
  }
}
