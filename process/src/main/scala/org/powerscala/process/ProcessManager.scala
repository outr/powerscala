package org.powerscala.process

import java.io.File
import org.powerscala.Unique
import org.powerscala.process.nativeprocess.NativeProcessInstance
import org.powerscala.process.classloader.ClassLoaderProcessInstance
import java.net.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ProcessManager {
  val Java = s"${System.getProperty("java.home")}/bin/java"
  val OS = System.getProperty("os.name")
  val Username = System.getenv("USERNAME")
  val PathSeparator = if (isWindows) ";" else ":"

  def isWindows = OS.toLowerCase.contains("windows")

  def native(name: String = Unique(),
             vmArgs: List[String] = Nil,
             className: String = null,
             args: List[String] = Nil,
             baseDirectory: File = new File("."),
             resources: List[File] = Nil) = {
    new NativeProcessInstance(name, vmArgs, className, args, baseDirectory, resources)
  }

  def local(name: String = Unique(),
                  className: String = null,
                  methodName: String = null,
                  args: Map[String, Any] = Map.empty,
                  resources: List[URL]) = {
    ClassLoaderProcessInstance(name, className, methodName, args, resources)
  }

  def main(args: Array[String]): Unit = {
    val jar = new File("/home/mhicks/test/test.jar")
//    val p = local("test", "org.hyperscala.site.HyperscalaSite", args = Map("args" -> Array.empty[String]), resources = List(jar.toURI.toURL))
    val p = native("test", className = "org.hyperscala.site.HyperscalaSite", baseDirectory = jar.getParentFile, resources = List(jar))
    println("Starting...")
    p.start(synchronous = false)
    println("...started!")
    println(s"${p.name} - ${p.state}")
    Thread.sleep(10000)
    println(s"${p.name} - ${p.state}")
    Thread.sleep(10000)
    p.stop()
  }
}
