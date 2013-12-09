package org.powerscala.process.classloader

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ClassLoaderProcess {
  def isRunning: Boolean

  def dispose(): Unit
}
