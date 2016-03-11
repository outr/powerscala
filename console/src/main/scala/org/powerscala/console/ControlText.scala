package org.powerscala.console

class ControlText(val content: String, val length: Int = 0) extends Text {
  def apply(text: Text*): GroupedText = GroupedText(Seq(this) ++ text ++ Seq(ctrl.Reset))

  override def toString: String = getClass.getSimpleName
}
