package org.powerscala.console

class ControlText(val content: String, val length: Int = 0) extends Text {
  def apply(text: Text*): GroupedText = GroupedText(List(this) ::: text.toList ::: List(ctrl.Reset))

  override def toString: String = getClass.getSimpleName
}
