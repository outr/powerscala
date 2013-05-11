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
class DirectoryWatcher(val path: Path) extends Listenable {
  def fileChange = FileChangeProcessor

  private val keepAlive = new AtomicBoolean(true)
  val watcher = FileSystems.getDefault.newWatchService()
  private val _started = new atomic.AtomicBoolean(false)
  def started = _started.get()

  path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)

  def start() = if (_started.compareAndSet(false, true)) {
    Executor.invoke {
      take()
    }
  }

  @tailrec
  private def take(): Unit = {
    if (keepAlive.get()) {
      val key = watcher.take()
      key.pollEvents().foreach {
        case event: WatchEvent[_] => {
          val change = event.kind() match {
            case ENTRY_CREATE => FileChange.Created
            case ENTRY_MODIFY => FileChange.Modified
            case ENTRY_DELETE => FileChange.Deleted
          }
          event.context() match {
            case filename: Path => fileChange.fire(FileChangeEvent(filename.toFile, change), this)
          }
        }
      }

      key.reset()
      take()
    }
  }

  def shutdown() = keepAlive.set(false)
}