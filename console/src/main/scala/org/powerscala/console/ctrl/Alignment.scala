package org.powerscala.console.ctrl

import org.powerscala.console.ControlText

sealed abstract class Alignment extends ControlText("")

object Alignment {
  case object Left extends Alignment
  case object Center extends Alignment
  case object Right extends Alignment
}