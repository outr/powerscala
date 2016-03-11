package org.powerscala.console

import org.powerscala.console.ctrl._

class Line(console: Console) {
  console.write(NewLine)

  def write(text: Text*): Unit = console.write(List(Return, CursorUp(1), ClearLine) ::: text.toList.filterNot(t => t.isInstanceOf[NewLine.type]) ::: List(NewLine): _*)
}
