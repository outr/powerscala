package org.powerscala.console

import org.powerscala.console.ctrl._

class Line(console: Console) {
  console.write(NewLine)

  def write(text: Text*): Unit = console.write(Seq(Return, CursorUp(1), ClearLine) ++ text.diff(Seq(NewLine)) ++ Seq(NewLine): _*)
}
