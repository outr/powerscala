package org.powerscala.hierarchy

import event.FileChangeEvent
import java.nio.file._
import StandardWatchEventKinds._
import org.powerscala.concurrent.Executor
import annotation.tailrec
import java.util.concurrent.atomic.AtomicBoolean
import collection.JavaConversions._
import org.powerscala.event.Listenable

/**
 * DirectoryWatcher watches the supplied path and fires FileChangeEvents when a file is created, modified, or deleted.
 *
 * The watcher invokes in a daemon thread to avoid blocking the initializing thread. Call shutdown() to stop monitoring.
 *
 * @author Matt Hicks <mhicks@outr.com>
 */
class DirectoryWatcher(path: Path) extends Listenable {
  private val keepAlive = new AtomicBoolean(true)
  val watcher = FileSystems.getDefault.newWatchService()

  path.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)

  Executor.invoke {
    take()
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
            case filename: Path => fire(new FileChangeEvent(filename.toFile, change))
          }
        }
      }

      key.reset()
      take()
    }
  }

  def shutdown() = keepAlive.set(false)
}