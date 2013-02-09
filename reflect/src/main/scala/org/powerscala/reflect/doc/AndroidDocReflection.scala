package org.powerscala.reflect.doc

import java.lang.reflect.{Constructor, Method}
import annotation.tailrec


/**
 * AndroidDocReflection adds support to process JavaDocs for Android API.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class AndroidDocReflection(className: String) extends DocumentationReflection {
  private lazy val url = AndroidDocReflection.baseURL + className.replaceAll("[.]", "/") + ".html"
  private lazy val string = DocumentationReflection.string(className, url)

  def method(m: Method) = {
    val nameLookup = generateNameLookup(m)
    val offset = string.indexOf("<A NAME=\"" + nameLookup + "\">")
    if (offset == -1) sys.error("Unable to find: " + nameLookup + " (" + m + ") in " + className)
    val offset2 = string.indexOf("<span class=\"normal\">", offset) + 1
    val argsString = Documentation.stripHTML(between(string, offset2, "<span class=\"normal\">", "</span>").get)
    try {
      val args = splitArgs(m.getParameterTypes.toList, argsString.substring(1, argsString.length - 1)).reverse
      val ret = DocumentedClass(null, m.getReturnType, None)

      val link = url + "#" + nameLookup
      MethodDocumentation(args, ret, link, None)
    } catch {
      case exc: IndexOutOfBoundsException => throw new RuntimeException("Method: " + m.getDeclaringClass.getName + "." + m.getName + " - " + argsString, exc)
    }
  }


  def constructor(c: Constructor[_]) = throw new UnsupportedOperationException("AndroidDocReflection does not support constructors")

  @tailrec
  private def splitArgs(paramTypes: List[Class[_]], s: String, args: List[DocumentedClass] = Nil): List[DocumentedClass] = {
    if (s.length == 0) {
      args
    } else {
      val end = s.indexOf(',') match {
        case -1 => s.length
        case i => i
      }
      val length = s.length
      val name = s.substring(0, end).split(" ")(1)
      val dc = DocumentedClass(name, paramTypes.head, None)
      val newString = if (s.length == end) {
        ""
      } else {
        s.substring(end + 2)
      }
      splitArgs(paramTypes.tail, newString, dc :: args)
    }
  }
}

object AndroidDocReflection extends DocMapper {
  var baseURL = "http://developer.android.com/reference/"

  def apply(c: Class[_]) = new AndroidDocReflection(c.getName)
}