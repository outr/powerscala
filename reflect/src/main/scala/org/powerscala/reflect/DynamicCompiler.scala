package org.powerscala.reflect

import java.io._
import com.twitter.util.Eval
import annotation.tailrec
import java.net.URL

/**
 * @author Matt Hicks <matt@outr.com>
 */
object DynamicCompiler {
  /**
   * Compiles the scala file provided and returns an Instantiator to create instances of specified className.
   *
   * @param className the class name within the source file that will be instantiated
   * @param url the source file to compile
   * @tparam T the generic type that will be created upon instantiation.
   * @return Instantiator[T]
   */
  def apply[T](className: String, url: URL): Instantiator[T] = {
    val temp = File.createTempFile("ReflectionCompiler", ".scala")
    try {
      val reader = new BufferedReader(new InputStreamReader(url.openStream()))
      try {
        val writer = new BufferedWriter(new FileWriter(temp))
        try {
          writer.write("import org.powerscala.reflect.Instantiator\n\n")
          stream(reader, writer)
          writer.write("new Instantiator[AnyRef] { def apply() = new %s() }".format(className))
        } finally {
          writer.flush()
          writer.close()
        }
        val eval = new Eval(None)
        eval[Instantiator[T]](temp)
      } finally {
        reader.close()
      }
    } finally {
      if (!temp.delete()) {
        println("Not deleted!")
        temp.deleteOnExit()
      }
    }
  }

  @tailrec
  private def stream(reader: BufferedReader, writer: BufferedWriter): Unit = {
    val line = reader.readLine()
    if (line != null) {
      writer.write(line)
      writer.write("\n")
      stream(reader, writer)
    }
  }
}

trait Instantiator[T] {
  def apply(): T
}