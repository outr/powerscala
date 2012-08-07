package org.powerscala.reflect.doc

import com.thoughtworks.paranamer.{ParameterNamesNotFoundException, BytecodeReadingParanamer, Paranamer}
import java.lang.RuntimeException
import scala.Some
import java.lang.reflect.{Constructor, Method}

import org.powerscala.reflect._

/**
 * ParanamerDocReflection uses Paranamer to introspect documentation details for classes and methods.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class ParanamerDocReflection(paranamer: Paranamer) extends DocumentationReflection {

  import ParanamerDocReflection.t2dc

  def method(m: Method) = {
    ParanamerDocReflection.predefined.get(m.toString) match {
      case Some(md) => md
      case None => try {
        val argNames = lookupParameterNames(m)
        val args = m.getParameterTypes.toList.zip(argNames).map(t2dc)
        val returnClass = DocumentedClass(null, m.getReturnType, None)
        MethodDocumentation(args, returnClass, null, None)
      } catch {
        case exc: ParameterNamesNotFoundException => throw new RuntimeException("Failure to retrieve method: " + m, exc)
      }
    }
  }

  def constructor(c: Constructor[_]) = throw new UnsupportedOperationException("ParanamerDocReflection does not support constructors")

  private def lookupParameterNames(m: Method) = {
    try {
      paranamer.lookupParameterNames(m, true)
    } catch {
      case exc: ParameterNamesNotFoundException => {
        val params = new Array[String](m.getParameterTypes.length)
        for (index <- 0 until params.length) {
          params(index) = "arg" + index
        }
        params
      }
    }
  }
}

object ParanamerDocReflection extends DocMapper {
  private implicit def t2dc(t: (Class[_], String)) = t match {
    case (c, s) => DocumentedClass(s, c, None)
  }

  private val equalsMethod = MethodDocumentation(List(classOf[AnyRef] -> "obj"), DocumentedClass(null, classOf[Boolean], None), null, None)
  private val waitMethod1 = MethodDocumentation(List(classOf[Long] -> "timeout"), DocumentedClass(null, classOf[Void], None), null, None)
  private val waitMethod2 = MethodDocumentation(List(classOf[Long] -> "timeout", classOf[Int] -> "nanos"), DocumentedClass(null, classOf[Void], None), null, None)
  private val predefined = Map(
    "public boolean java.lang.Object.equals(java.lang.Object)" -> equalsMethod,
    "public final native void java.lang.Object.wait(long) throws java.lang.InterruptedException" -> waitMethod1,
    "public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException" -> waitMethod2
  )

  lazy val default = new ParanamerDocReflection(new BytecodeReadingParanamer())

  def apply(c: Class[_]) = default
}