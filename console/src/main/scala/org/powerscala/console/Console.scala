package org.powerscala.console

import jline.TerminalFactory
import org.powerscala.console.ctrl._

import scala.collection.mutable.ListBuffer

class Console(writer: String => Unit) {
  def width: Int = TerminalFactory.get.getWidth

  def write(text: Text*): Unit = {
    val blocks = ListBuffer.empty[List[Text]]
    var block = ListBuffer.empty[Text]
    text.foreach { t =>
      t match {
        case a: Alignment if block.nonEmpty => {
          blocks += block.toList
          block.clear()
        }
        case _ => // Ignore
      }
      block += t
    }
    if (block.nonEmpty) {
      blocks += block.toList
      block.clear()
    }

    blocks.toList.foreach { b =>
      val length = b.foldLeft(0)((len, t) => len + t.length)

      b.foreach {
        case ClearLine => {
          writer(s"${SavePosition.content}${Return.content}${EraseLine.content}${RestorePosition.content}")
        }
        case Alignment.Left => writer(Return.content)
        case Alignment.Center => writer(s"${Return.content}${CursorForward(math.round((width.toDouble / 2.0) - (length.toDouble / 2.0)).toInt).content}")
        case Alignment.Right => writer(s"${Return.content}${CursorForward(width - length).content}")
        case t => writer(t.content)
      }
    }
  }

  def writeLine(text: Text*): Unit = write(text.toList ::: List(NewLine): _*)

  def line(): Line = new Line(this)

  def repeating(c: Char, length: Int): String = {
    val b = new StringBuilder
    (0 until length).foreach { index =>
      b.append(c)
    }
    b.toString()
  }
}

object Console extends Console((s: String) => print(s))