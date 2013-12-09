package org.powerscala.process

import java.io.File
import org.powerscala.{IO, Unique}
import org.powerscala.process.nativeprocess.NativeProcessInstance
import org.powerscala.process.classloader.ClassLoaderProcessInstance

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
             resources: List[File] = Nil,
             resourcesDirectory: Option[File] = None) = {
    new NativeProcessInstance(name, vmArgs, className, args, baseDirectory, remap(resources, resourcesDirectory))
  }

  def local(name: String = Unique(),
            className: String = null,
            methodName: String = null,
            args: Map[String, Any] = Map.empty,
            resources: List[File],
            resourcesDirectory: Option[File] = None) = {
    ClassLoaderProcessInstance(name, className, methodName, args, remap(resources, resourcesDirectory))
  }

  private def remap(resources: List[File], resourcesDirectory: Option[File]) = resourcesDirectory match {
    case Some(dir) => {
      dir.mkdirs()
      resources.map {
        case f => {
          val tmp = new File(dir, f.getName)
          if (!tmp.exists() || tmp.lastModified() != f.lastModified()) {
            IO.copy(f, tmp)                         // Copy the file to the directory
            tmp.setLastModified(f.lastModified())   // Update the last modified date to be the same
          }
          tmp
        }
      }
    }
    case None => resources
  }
}
