package org.powerscala.console

case class StringText(content: String) extends Text {
  lazy val length: Int = content.length
}
