package org.powerscala.console

import jline.TerminalFactory

import scala.sys.process._

import scala.{Console => ScalaConsole}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Console {
  object Color {
    def Black = ScalaConsole.BLACK
    def Blue = ScalaConsole.BLUE
    def Cyan = ScalaConsole.CYAN
    def Green = ScalaConsole.GREEN
    def Magenta = ScalaConsole.MAGENTA
    def Red = ScalaConsole.RED
    def White = ScalaConsole.WHITE
    def Yellow = ScalaConsole.YELLOW
  }
  object Background {
    def Black = ScalaConsole.BLACK_B
    def Blue = ScalaConsole.BLUE_B
    def Cyan = ScalaConsole.CYAN_B
    def Green = ScalaConsole.GREEN_B
    def Magenta = ScalaConsole.MAGENTA_B
    def Red = ScalaConsole.RED_B
    def White = ScalaConsole.WHITE_B
    def Yellow = ScalaConsole.YELLOW_B
  }
  object Effect {
    def Blink = ScalaConsole.BLINK
    def Bold = ScalaConsole.BOLD
    def Invisible = ScalaConsole.INVISIBLE
    def Reset = ScalaConsole.RESET
    def Reversed = ScalaConsole.REVERSED
    def Underlined = ScalaConsole.UNDERLINED
  }
  object Character {
    val NewLine = "\n"
    val Tab = "\t"
    val Backspace = "\b"
    val Return = "\r"
    val FormFeed = "\f"
  }

  def width = TerminalFactory.get().getWidth

  def line(s: Any*) = {
    val w = width
    val joined = s.mkString("").take(w).padTo(w, ' ')
    System.out.print(joined)
  }

  def replace(s: Any*) = {
    System.out.print(Character.Return)
    line(s: _*)
  }

  def println(s: Any*) = {
    val joined = s.mkString("")
    System.out.println(joined)
  }
}