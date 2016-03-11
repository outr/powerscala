package org.powerscala.console

case class GroupedText(items: Seq[Text]) extends Text {
  lazy val content: String = items.map(_.content).mkString
  lazy val length: Int = items.foldLeft(0)((len, t) => len + t.length)
}