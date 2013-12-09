package org.powerscala.process.nativeprocess

import org.powerscala.process.{ProcessManager, ProcessInstance}
import java.io.{FileNotFoundException, File}
import scala.collection.mutable.ListBuffer
import scala.sys.process.{ProcessLogger, Process}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class NativeProcessInstance(val name: String,
                            vmArgs: List[String],
                            className: String,
                            args: List[String],
                            baseDirectory: File,
                            resources: List[File]) extends ProcessInstance with ProcessLogger {
  private val processBuilder = buildProcess()
  private var process: Process = null
  private var _exitValue = -1

  def exitValue = _exitValue

  def run() = {
    process = processBuilder.run(this)
    _exitValue = process.exitValue()
  }

  def stop() = {
    process.destroy()
    true
  }

  private def buildProcess() = {
    resources.foreach(f => if (!f.exists()) throw new FileNotFoundException(s"Resource does not exist: ${f.getAbsolutePath}"))

    val b = ListBuffer.empty[String]
    b += ProcessManager.Java
    vmArgs.foreach(arg => b += arg)
    b += s"-Duser.dir=${baseDirectory.getAbsolutePath}"
    b += "-cp"
    b += resources.map(f => f.getAbsolutePath).mkString(ProcessManager.PathSeparator)
    b += className
    args.foreach(arg => b += arg)
    println(b.mkString(" "))

    Process(b.toList)
  }

  def out(s: => String) = System.out.println(s"$name: $s")

  def err(s: => String) = System.err.println(s"$name: $s")

  def buffer[T](f: => T): T = f
}