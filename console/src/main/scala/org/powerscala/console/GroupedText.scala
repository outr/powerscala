package org.powerscala.console

case class GroupedText(items: Seq[Text]) extends Text {
  lazy val content: String = items.map(_.content).mkString
  lazy val length: Int = items.map(_.length).sum
}