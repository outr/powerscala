package org.powerscala.log.slf4j.impl

import org.slf4j.spi.LoggerFactoryBinder

/**
 * @author Matt Hicks <matt@outr.com>
 */
class StaticLoggerBinder extends LoggerFactoryBinder {
  def getLoggerFactory = PSLoggerFactory

  def getLoggerFactoryClassStr = PSLoggerFactory.getClass.getName
}