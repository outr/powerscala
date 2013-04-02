package org.powerscala.convert

import language.implicitConversions

/**
 * @author Matt Hicks <matt@outr.com>
 */
package object string {
  implicit def int2String(i: Int) = i.toString
  implicit def string2Int(s: String) = try {
    s.toInt
  } catch {
    case t: Throwable => 0
  }
}
