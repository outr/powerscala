package org.powerscala.reflect.doc

import java.util.concurrent.ConcurrentHashMap
import annotation.tailrec
import io.Source
import java.net.URL
import java.lang.reflect.{Constructor, Method}

/**
 * Implementations of DocumentationReflection provide functionality to introspect the JavaDoc /
 * ScalaDoc for a Class.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DocumentationReflection {
  /**
   * Looks up MethodDocumentation for the supplied method.
   */
  def method(m: Method): MethodDocumentation

  /**
   * Looks up MethodDocumentation for the supplied constructor.
   */
  def constructor(c: Constructor[_]): MethodDocumentation

  protected def between(string: String, offset: Int, pre: String, post: String) = {
    val start = string.indexOf(pre, offset)
    if (start != -1) {
      val begin = start + pre.length
      val end = string.indexOf(post, begin)
      if (end != -1 && end - begin > 0) {
        Some(string.substring(begin, end).trim)
      } else {
        None
      }
    } else {
      None
    }
  }

  protected def generateNameLookup(m: Method) = {
    val nameLookup = new StringBuilder
    nameLookup.append(m.getName)
    nameLookup.append('(')
    for ((c, index) <- m.getParameterTypes.zipWithIndex) {
      if (index > 0) nameLookup.append(", ")
      nameLookup.append(JavaDocReflection.typeConversion(c))
    }
    nameLookup.append(')')
    nameLookup.toString()
  }

  protected def cleanWhite(s: String) = {
    val b = new StringBuilder
    var white = false
    for (c <- s) {
      if (c.isWhitespace) {
        if (white) {
          // Ignore
        } else {
          white = true
          b.append(' ')
        }
      } else {
        white = false
        b.append(c)
      }
    }
    b.toString
  }
}

object DocumentationReflection {
  /**
   * Determines whether source lookups will look remotely if no locally cached documentation is found.
   *
   * Defaults to true.
   */
  var remoteSources = true
  private var packageMapping = new ConcurrentHashMap[String, DocMapper]

  register("java", JavaDocReflection)
  register("javax", JavaDocReflection)
  register("javax.microedition", AndroidDocReflection)
  register("android", AndroidDocReflection)

  /**
   * Registers a DocMapper to a base package.
   */
  def register(packageBase: String, docMapper: DocMapper) = packageMapping.put(packageBase, docMapper)

  /**
   * Looks up the DocumentationReflection object for the specific class.
   */
  def apply(c: Class[_]) = findMatch(c.getName).apply(c)

  @tailrec
  private def findMatch(s: String): DocMapper = packageMapping.get(s) match {
    case null => {
      val index = s.lastIndexOf('.')
      if (index == -1) {
//        ParanamerDocReflection // If nothing else registered, we use ParanamerReflection
        ASMDocReflection
      } else {
        findMatch(s.substring(0, index))
      }
    }
    case docMapper => docMapper
  }

  def source(className: String, urlString: String) = {
    getClass.getClassLoader.getResource(className + ".html") match {
      case null => if (remoteSources) {
        Source.fromURL(new URL(urlString), "UTF-8")
      } else {
        throw new RuntimeException("Unable to find local copy of: " + className + ".html")
      }
      case url => Source.fromURL(url, "UTF-8")
    }
  }
}