package org.powerscala.console.ctrl

import org.powerscala.console.ControlText

case class CursorBack(lines: Int = 1) extends ControlText(s"\033[${lines}D")