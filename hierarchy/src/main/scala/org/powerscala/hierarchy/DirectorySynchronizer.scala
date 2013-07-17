package org.powerscala.hierarchy

import java.io.File
import org.powerscala.hierarchy.event.{FileChangeProcessor, FileChangeEvent}
import java.util.concurrent.ConcurrentLinkedQueue
import org.powerscala.concurrent.Executor
import scala.annotation.tailrec
import org.powerscala.IO
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class DirectorySynchronizer(source: File, destination: File, sync: Boolean) extends Listenable {
  val fileChange = new FileChangeProcessor

  private val watcher = new DirectoryWatcher(source.toPath, true)
  private val queue = new ConcurrentLinkedQueue[File]()

  watcher.fileChange.on(fileChanged)

  def start() = {
    if (sync) {
      synchronizeDirectory(source)
      synchronizeReverse(destination)
    }
    watcher.start()
    Executor.scheduleWithFixedDelay(0.0, 1.0) {
      processQueue()
    }
  }

  private def fileChanged(evt: FileChangeEvent): Unit = {
    queue.add(evt.file)
  }

  @tailrec
  private def processQueue(): Unit = {
    val file = queue.poll()
    if (file != null) {
      synchronize(file)

      processQueue()
    }
  }

  private def synchronizeDirectory(directory: File): Unit = {
    directory.listFiles().foreach {
      case file => {
        synchronize(file)
        if (file.isDirectory) {
          synchronizeDirectory(file)
        }
      }
    }
  }

  private def synchronizeReverse(directory: File): Unit = {
    directory.listFiles().foreach {
      case file => {
        val s = convertToSource(file)
        if (!s.exists()) {
          file.delete()
        }
        if (file.isDirectory) {
          synchronizeReverse(file)
        }
      }
    }
  }

  private def synchronize(file: File) = {
    val dest = convertToDestination(file)
    if (file.isDirectory) {
      if (!dest.isDirectory) {
        dest.mkdirs()
        dest.setLastModified(file.lastModified())
        fileChange.fire(new FileChangeEvent(file, FileChange.Created))
        synchronizeDirectory(file)
      }
    } else if (file.exists()) {
      val change = if (dest.exists()) FileChange.Modified else FileChange.Created
      IO.copy(file, dest)
      dest.setLastModified(file.lastModified())
      fileChange.fire(new FileChangeEvent(file, change))
    } else {
      IO.delete(dest)
      fileChange.fire(new FileChangeEvent(file, FileChange.Deleted))
    }
  }

  private def convertToDestination(file: File) = {
    val path = file.getAbsolutePath.substring(source.getAbsolutePath.length)
    new File(destination, path)
  }

  private def convertToSource(file: File) = {
    val path = file.getAbsolutePath.substring(destination.getAbsolutePath.length)
    new File(source, path)
  }
}

object DirectorySynchronizer {
  def main(args: Array[String]): Unit = {
    val source = new File("source")
    val destination = new File("destination")
    val sync = new DirectorySynchronizer(source, destination, sync = true)
    sync.fileChange.on {
      case evt => println(s"FileChanged: $evt")
    }
    sync.start()

    Thread.sleep(1000000)
  }
}