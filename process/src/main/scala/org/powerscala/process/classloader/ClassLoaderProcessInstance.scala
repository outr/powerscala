package org.powerscala.process.classloader

import java.net.{URL, URLClassLoader}
import org.powerscala.reflect.{EnhancedMethod, EnhancedClass}
import org.powerscala.process.ProcessInstance

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ClassLoaderProcessInstance(val name: String,
                                 classLoader: URLClassLoader,
                                 clazz: EnhancedClass,
                                 method: EnhancedMethod,
                                 args: Map[String, Any]) extends ProcessInstance {
  if (method == null) throw new NullPointerException("Unable to find method!")

  private lazy val companion = clazz.instance.getOrElse(throw new NullPointerException(s"Unable to find companion object for $clazz"))
  private lazy val disposeMethod = clazz.methodByName("dispose")
  private lazy val isRunningMethod = clazz.methodByName("isRunning")

  def run() = contextualize {
    method[Unit](companion, args)
    isRunningMethod match {         // If an "isRunning" method exists, wait for it to return false
      case Some(m) => while(m[Boolean](companion)) {
        Thread.sleep(500)
      }
      case None => // No "isRunning" method found, so presume that execution returning means it's finished
    }
  }

  def contextualize[T](f: => T): T = {
    val previousClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(classLoader)
    try {
      f
    } finally {
      Thread.currentThread().setContextClassLoader(previousClassLoader)
    }
  }

  def stop() = contextualize {
    disposeMethod match {
      case Some(dispose) => {
        dispose[Unit](companion)
        true
      }
      case None => false
    }
  }
}

object ClassLoaderProcessInstance {
  def apply(name: String,
            className: String,
            methodName: String,
            args: Map[String, Any],
            resources: List[URL]) = {
    val classLoader = new URLClassLoader(resources.toArray, null)
    val previousClassLoader = Thread.currentThread().getContextClassLoader
    Thread.currentThread().setContextClassLoader(classLoader)
    val clazz: EnhancedClass = classLoader.loadClass(className)
    val mn = methodName match {
      case null => "main"
      case _ => methodName
    }
    val method = clazz.methodByName(mn).getOrElse(throw new RuntimeException(s"Unable to find method: $mn in $clazz"))
    Thread.currentThread().setContextClassLoader(previousClassLoader)
    new ClassLoaderProcessInstance(name, classLoader, clazz, method, args)
  }
}