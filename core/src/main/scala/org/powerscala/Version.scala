package org.powerscala

/**
 * Version represents a version numbering.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class Version(major: Int = 1, minor: Int = 0, maintenance: Int = 0, build: Int = 0) {
  override def toString = "%s.%s.%s.%s".format(major, minor, maintenance, build)
}