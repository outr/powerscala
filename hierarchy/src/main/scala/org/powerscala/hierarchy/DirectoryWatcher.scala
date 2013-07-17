package org.powerscala.hierarchy

import org.powerscala.hierarchy.event.{FileChangeProcessor, FileChangeEvent}
import java.nio.file._
import StandardWatchEventKinds._
import org.powerscala.concurrent.Executor
import annotation.tailrec
import java.util.concurrent.atomic.AtomicBoolean
import collection.JavaConversions._
import org.powerscala.event.Listenable
import java.util.concurrent.atomic

/**
 * DirectoryWatcher watches the supplied path and fires FileChangeEvents when a file is created, modified, or deleted.
 *
 * The watcher invokes in a daemon thread to avoid blocking the initializing thread. Call shutdown() to stop monitoring.
 *
 * @author Matt Hicks <mhicks@outr.com>
 */
class DirectoryWatcher(val directory: Path, recursive: Boolean) extends Listenable {
  val fileChange = new FileChangeProcessor

  private val keepAlive = new AtomicBoolean(true)
  val watcher = FileSystems.getDefault.newWatchService()
  private val _started = new atomic.AtomicBoolean(false)
  private var keys = Map.empty[WatchKey, Path]
  def started = _started.get()

  registerDirectory(directory)

  def start() = if (_started.compareAndSet(false, true)) {
    Executor.invoke {
      take()
    }
  }

  def registerDirectory(dir: Path): Unit = {
    val key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)
    keys += key -> dir
    if (recursive) {
      dir.toFile.listFiles().foreach {
        case d if d.isDirectory => registerDirectory(d.toPath)
        case _ => // Ignore files
      }
    }
  }

  @tailrec
  private def take(): Unit = {
    if (keepAlive.get()) {
      val key = watcher.take()
      val dir = keys(key)
      key.pollEvents().foreach {
        case event: WatchEvent[_] => {
          val change = event.kind() match {
            case ENTRY_CREATE => FileChange.Created
            case ENTRY_MODIFY => FileChange.Modified
            case ENTRY_DELETE => FileChange.Deleted
          }
          event.context() match {
            case filename: Path => {
              val child = dir.resolve(filename)

              val file = child.toAbsolutePath.toFile
              fileChange.fire(FileChangeEvent(file, change))
              if (recursive && change == FileChange.Created && file.isDirectory) {
                registerDirectory(child)   // Register the newly created directory
              }
            }
          }
        }
      }

      key.reset()
      take()
    }
  }

  def shutdown() = keepAlive.set(false)
}