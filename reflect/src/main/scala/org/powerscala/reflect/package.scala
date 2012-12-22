package org.powerscala

import java.lang.reflect.Method
import scala.ref.SoftReference
import java.util.concurrent.ConcurrentHashMap

package object reflect {
  private val map = new ConcurrentHashMap[Class[_], SoftReference[EnhancedClass]]

  protected[reflect] def register(c: Class[_], ec: EnhancedClass) = {
    map.put(c, new SoftReference[EnhancedClass](ec))
    ec
  }

  implicit def class2EnhancedClass(c: Class[_]): EnhancedClass = if (c == null) {
    null
  } else {
    map.get(c) match {
      case null => registerEnhancedClass(c)
      case ref if (!ref.isEnqueued) => ref.get match {
        case Some(ec) => ec
        case None => registerEnhancedClass(c)
      }
    }
  }

  private def registerEnhancedClass(c: Class[_]) = {
    val ec = new EnhancedClass(c)
    register(c, ec)
  }

  implicit def method2EnhancedMethod(m: Method) = class2EnhancedClass(m.getDeclaringClass)(m)

  implicit def f0ToEM(f: Function0[_]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 0)
  }

  implicit def f1ToEM(f: Function1[_, _]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 1)
  }

  implicit def f2ToEM(f: Function2[_, _, _]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 2)
  }

  implicit def f3ToEM(f: Function3[_, _, _, _]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 3)
  }

  implicit def f4ToEM(f: Function4[_, _, _, _, _]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 4)
  }

  implicit def f5ToEM(f: Function5[_, _, _, _, _, _]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 5)
  }

  implicit def f6ToEM(f: Function6[_, _, _, _, _, _, _]) = {
    class2EnhancedClass(f.getClass).methods.find(m => m.name == "apply" && m.args.length == 6)
  }

  /**
   * Finds a method based on the absolute signature including class.
   */
  def method(absoluteSignature: String) = {
    val absoluteMethodName = absoluteSignature.substring(0, absoluteSignature.indexOf('('))
    val className = absoluteMethodName.substring(0, absoluteMethodName.lastIndexOf('.'))
    val c = Class.forName(className)
    c.methodBySignature(absoluteSignature)
  }
}