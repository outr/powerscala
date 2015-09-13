package org.powerscala.console

import akka.actor.{Props, ActorSystem, Actor}
import jline.TerminalFactory

import scala.{Console => ScalaConsole}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object Console {
  def main(args: Array[String]): Unit = {
    write(Character.NewLine, Control.Padding('.'), Color.Red, "Hello", Effect.Reset, Control.AlignRight, "World!")
  }

  var writer: String => Unit = new SynchronizedWriter(SystemOutWriter)

  case class ControlString(value: String) {
    override def toString = value
  }

  object Color {
    val Black = ControlString(ScalaConsole.BLACK)
    val Blue = ControlString(ScalaConsole.BLUE)
    val Cyan = ControlString(ScalaConsole.CYAN)
    val Green = ControlString(ScalaConsole.GREEN)
    val Magenta = ControlString(ScalaConsole.MAGENTA)
    val Red = ControlString(ScalaConsole.RED)
    val White = ControlString(ScalaConsole.WHITE)
    val Yellow = ControlString(ScalaConsole.YELLOW)
  }
  object Background {
    val Black = ControlString(ScalaConsole.BLACK_B)
    val Blue = ControlString(ScalaConsole.BLUE_B)
    val Cyan = ControlString(ScalaConsole.CYAN_B)
    val Green = ControlString(ScalaConsole.GREEN_B)
    val Magenta = ControlString(ScalaConsole.MAGENTA_B)
    val Red = ControlString(ScalaConsole.RED_B)
    val White = ControlString(ScalaConsole.WHITE_B)
    val Yellow = ControlString(ScalaConsole.YELLOW_B)
  }
  object Effect {
    val Blink = ControlString(ScalaConsole.BLINK)
    val Bold = ControlString(ScalaConsole.BOLD)
    val Invisible = ControlString(ScalaConsole.INVISIBLE)
    val Reset = ControlString(ScalaConsole.RESET)
    val Reversed = ControlString(ScalaConsole.REVERSED)
    val Underlined = ControlString(ScalaConsole.UNDERLINED)
  }
  object Character {
    val NewLine = "\n"
    val Tab = "\t"
    val Backspace = "\b"
    val Return = "\r"
    val FormFeed = "\f"
  }
  object Control {
    /**
     * Used to define that all following characters should apply to the right side instead of the left
     */
    object AlignRight extends Control

    /**
     * Defines how padding should occur on the line
     *
     * @param char the character to use
     */
    case class Padding(char: Char) extends Control
  }
  trait Control {
    override def toString = ""  // They do special things, they don't output anything
  }

  def width = TerminalFactory.get().getWidth

  def write(s: Any*) = {
    val maxWidth = width
    val (left, right) = s.flatMap {
      case c: Control => List(c)
      case cs: ControlString => List(cs)
      case v => v.toString.toList
    }.span(e => e != Control.AlignRight)

    val padChar = s.collectFirst {
      case Control.Padding(char) => char
    }.getOrElse(' ')

    val array = new Array[Any](maxWidth)

    // Fill with padding character
    (0 until maxWidth).foreach { index =>
      array(index) = padChar
    }

    // Fill left
    var index = 0
    left.foreach { c =>
      array(index) = c
      index += 1
    }

    // Fill right
    index = maxWidth - 1
    right.reverse.foreach { c =>
      array(index) = c
      index -= 1
    }

    val result = array.map {
      case c: Control => padChar.toString
      case v => v.toString
    }.mkString("")
    writer(result)
  }

  def replace(s: Any*) = {
    writer(Character.Return)
    write(s: _*)
  }

  def println(s: Any*) = {
    val joined = s.mkString("")
    writer(joined)
  }
}

class SynchronizedWriter(val writer: String => Unit) extends (String => Unit) {
  override def apply(text: String) = synchronized {
    writer(text)
  }
}

class ActorWriter(val writer: String => Unit) extends (String => Unit) {
  override def apply(text: String) = ActorWriter.actor ! new ActorText(writer, text)
}

object ActorWriter {
  val system = ActorSystem("ActorWriter")
  val actor = system.actorOf(Props[ActorWriterActor], name = "stream")
}

class ActorText(val writer: String => Unit, val text: String)

class ActorWriterActor extends Actor {
  override def receive = {
    case text: ActorText => text.writer(text.text)
  }
}

object SystemOutWriter extends (String => Unit) {
  override def apply(text: String) = System.out.print(text)
}