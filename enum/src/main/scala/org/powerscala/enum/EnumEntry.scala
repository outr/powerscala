package org.powerscala.enum

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EnumEntry {
  lazy val name = getClass.getSimpleName.substring(0, getClass.getSimpleName.length - 1)
}