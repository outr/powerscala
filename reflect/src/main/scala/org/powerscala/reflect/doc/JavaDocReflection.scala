package org.powerscala.reflect.doc

import java.net.URL
import io.Source
import java.lang.reflect.{Constructor, Method}

import org.powerscala.reflect._

/**
 * JavaDocReflection uses JavaDocs to parse information to determine documentation about classes and
 * methods.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class JavaDocReflection(className: String) extends DocumentationReflection {
  private lazy val url = JavaDocReflection.baseURL + className.replaceAll("[.]", "/") + ".html"
  private lazy val string = {
    val source = Source.fromURL(new URL(url), "UTF-8")
    try {
      source.mkString
    } finally {
      source.close()
    }
  }

  def method(m: Method): MethodDocumentation = {
    assert(m.getDeclaringClass.getName == className, "Attempting to load documentation for %s.%s on the wrong class: %s".format(m.getDeclaringClass.getName, m.getName, className))
    val nameLookup = generateNameLookup(m)
    val start = string.indexOf("<A NAME=\"" + nameLookup + "\">")
    if (start == -1) {
      val args = genericArgs(m)
      MethodDocumentation(args, DocumentedClass(null, m.getReturnType, None), null, None)
    } else {
      val end = string.indexOf("<A NAME=\"", start + 1) match {
        case -1 => string.length
        case pos => pos
      }
      val content = string.substring(start, end)

      // Documentation
      val doc = between(content, 0, "<DD>", "<DD>").map(s => Documentation(cleanWhite(s)))

      // Arguments
      val args: List[DocumentedClass] = if (m.getParameterTypes.length == 0) {
        Nil
      } else {
        val paramStart = content.indexOf("<DT><B>Parameters:</B><DD>") + "<DT><B>Parameters:</B><DD>".length()
        if (paramStart == -1) throw new RuntimeException("Unable to find beginning of parameters: [%s]".format(content))
        val paramEnd = content.indexOf("<DT>", paramStart + 4) match {
          case -1 => content.length
          case pos => pos
        }
        val paramContent = content.substring(paramStart, paramEnd)
        val blocks = paramContent.split("<DD>")
        if (blocks.length != m.getParameterTypes.length) {
          //          throw new RuntimeException("Unable to parse parameter content properly for method %s.%s: [%s] and [%s]".format(m.getDeclaringClass.getName, m.getName, paramContent, content))
          genericArgs(m)
        } else {
          blocks.zipWithIndex.map(t => parseArg(t._1, t._2, m.getParameterTypes()(t._2))).toList
        }
      }

      // Return
      val retDocs = (between(content, 0, "<DT><B>Returns:</B><DD>", "<DT>") match {
        case Some(docs) => Some(docs)
        case None => between(content, 0, "<DT><B>Returns:</B><DD>", "</DL>")
      }).map(s => Documentation(cleanWhite(s)))
      val ret = DocumentedClass(null, m.getReturnType, retDocs)
      val link = url + "#" + nameLookup
      MethodDocumentation(args, ret, link, doc)
    }
  }

  def constructor(c: Constructor[_]) = throw new UnsupportedOperationException("JavaDocReflection does not support constructors")

  private val parseArg = (content: String, index: Int, clazz: Class[_]) => {
    val name = between(content, 0, "<CODE>", "</CODE>").getOrElse("")
    val doc = content.substring(content.indexOf("</CODE>") + 10).trim match {
      case "" => None
      case sub => Some(cleanWhite(sub))
    }
    DocumentedClass(name, clazz, doc.map(s => Documentation(s)))
  }

  private def genericArgs(m: Method) = {
    m.getParameterTypes.zipWithIndex.map(t => DocumentedClass("arg%s".format(t._2), t._1, None)).toList
  }

  /*def method(m: Method) = {
    assert(m.getDeclaringClass.getName == className, "Attempting to load documentation for %s.%s on the wrong class: %s".format(m.getDeclaringClass.getName, m.getName, className))
    val nameLookup = generateNameLookup(m)
    val offset = string.indexOf("<A NAME=\"" + nameLookup + "\">")
    if (offset == -1) {
      val args = m.getParameterTypes.zipWithIndex.map(t => DocumentedClass("arg%s".format(t._2), t._1, None)).toList
      MethodDocumentation(args, DocumentedClass(null, m.getReturnType, None), null, None)
    } else {
      val doc = between(string, offset, "<DD>", "<DD>").map(s => Documentation(cleanWhite(s)))
      val args = if (m.getParameterTypes.length == 0) {
        Nil
      } else {
        between(string, offset, "<DT><B>Parameters:</B>", "<DT><B>") match {
          case Some(s) => try {
            m.getParameterTypes.toList match {
              case Nil => Nil
              case list => splitArgs(list, s).reverse
            }
          } catch {
            case exc => throw new RuntimeException("Failed to split arguments for %s.%s with data: %s".format(className, m.getName, s), exc)
          }
          case None => Nil
        }
      }
      val retDocs = (between(string, offset, "<DT><B>Returns:</B><DD>", "<DT>") match {
        case Some(docs) => Some(docs)
        case None => between(string, offset, "<DT><B>Returns:</B><DD>", "</DL>")
      }).map(s => Documentation(cleanWhite(s)))
      val ret = DocumentedClass(null, m.getReturnType, retDocs)

      val link = url + "#" + nameLookup
      MethodDocumentation(args, ret, link, doc)
    }
  }

  @tailrec
  private def splitArgs(paramTypes: List[Class[_]], s: String, args: List[DocumentedClass] = Nil): List[DocumentedClass] = {
    val break = s.indexOf("<DD>", 5) match {
      case -1 => s.length
      case i => i
    }
    val content = s.substring(4, break)
    val end = content.indexOf("</CODE>")
    if (end == -1) throw new RuntimeException("Unable to parse argument for %s".format(s))
    val name = content.substring(6, end)
    val doc = content.substring(content.indexOf("</CODE>") + 10).trim match {
      case "" => None
      case sub => Some(cleanWhite(sub))
    }
    val dc = DocumentedClass(name, paramTypes.head, doc.map(s => Documentation(s)))
    val updated = dc :: args
    if (s.length == break) {
      updated
    } else {
      splitArgs(paramTypes.tail, s.substring(break), updated)
    }
  }*/
}

object JavaDocReflection extends DocMapper {
  var baseURL = "http://download.oracle.com/javase/6/docs/api/"

  def typeConversion(c: Class[_]) = c.getName match {
    case "[I" => "int[]"
    case "[L" => "long[]"
    case "[F" => "float[]"
    case "[D" => "double[]"
    case s => s
  }

  def apply(c: Class[_]) = new JavaDocReflection(c.getName)
}