package org.powerscala.convert.json

import org.powerscala.convert.{CaseClassValue, CaseClass, ConversionBus}
import org.powerscala.EnumEntry
import annotation.tailrec
import util.parsing.json.JSONFormat

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object Object2JSON extends ConversionBus {
  def toJSON(ref: Any): String = ref match {
    case null => null
    case s: String => s
    case e: EnumEntry[_] => quotedString(e.name)
    case s: Seq[_] => s.map(v => toJSON(v)).mkString("[", ", ", "]")
    case _ => convert(ref) match {
      case caseClass: CaseClass => cc2json(caseClass)
      case result => result.toString
    }
  }

  private def cc2json(caseClass: CaseClass) = new CaseClass2JSON(caseClass).json

  def quotedString(s: String) = "\"" + JSONFormat.quoteString(s) + "\"" //"\"%s\"".format(s.replace("\"", "\\\""))
}

class CaseClass2JSON(caseClass: CaseClass) {
  private var depth = 0
  private val b = new StringBuilder

  import Object2JSON._

  writeCaseClass(caseClass)

  private def writeCaseClass(caseClass: CaseClass): Unit = {
    writeLine("{")
    depth += 1
    writeAttributes(caseClass.values)
    tabs()
    writeLine("\"class\": \"%s\"".format(caseClass.clazz.name))
    depth -= 1
    tabs()
    write("}")
  }

  @tailrec
  private def writeAttributes(values: List[CaseClassValue]): Unit = {
    if (values.nonEmpty) {
      val ccv = values.head
      tabs()
      write("\"%s\": ".format(ccv.caseValue.name))
      writeValue(ccv.value)
      writeLine(",")
      writeAttributes(values.tail)
    }
  }

  private def writeValue(v: Any): Unit = v match {
    case null => write("null")
    case s: Seq[_] => {
      writeLine("[")
      var first = true
      s.foreach {
        case sv => {
          if (first) {
            first = false
          } else {
            writeLine(",")
          }
          sv match {
            case s: String => write(quotedString(s))
            case _ => write(Object2JSON.toJSON(sv))
          }
        }
      }
      writeLine("]")
    }
    case e: EnumEntry[_] => write("\"%s\"".format(e.name))
    case b: Boolean => write(b.toString)
    case b: Byte => write(b.toString)
    case s: Short => write(s.toString)
    case i: Int => write(i.toString)
    case l: Long => write(l.toString)
    case f: Float => write(f.toString)
    case d: Double => write(d.toString)
    case s: String => write(quotedString(s))
    case caseClass: CaseClass => writeCaseClass(caseClass)
    case value => {
      //          println("Writing to string for: %s / %s".format(value.asInstanceOf[AnyRef].getClass.getName, value))
      write(quotedString(value.toString))
    }
  }

  private def tabs() = {
    for (i <- 0 until depth) {
      write("\t")
    }
  }

  private def writeLine(s: String = "") = {
    write(s)
    write("\r\n")
  }
  private def write(s: String) = b.append(s)

  def json = b.toString()
}