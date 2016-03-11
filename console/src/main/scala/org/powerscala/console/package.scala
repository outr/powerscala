package org.powerscala

import scala.language.implicitConversions

package object console {
  implicit def string2Text(s: String): Text = StringText(s)
}