package org.powerscala.event.concurrent

import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

import org.powerscala.event.Listenable
import org.powerscala.event.processor.UnitProcessor

import scala.concurrent.{ExecutionContext, Future}

/**
 * Task is very similar to Callable, Future, Runnable, etc. except that it provides more depth of the current state of
 * the progress.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait Task[T] extends Listenable {
  val statusChanged = new UnitProcessor[Status]("status")

  def manager: TaskManager
  protected def run(update: Status => Unit): T

  final def start() = {
    manager._tasks.add(this)
    manager.queued.fire(this)
    try {
      Future {
        manager.started.fire(this)
        val updateStatus = (status: Status) => {
          statusChanged.fire(status)
          manager.status.fire(this -> status)
        }
        val result = run(updateStatus)
        manager.completed.fire(this -> result)
        result
      }(manager.executionContext)
    } finally {
      manager._tasks.remove(this)
      manager.finished.fire(this)
    }
  }
}

trait TaskManager extends Listenable {
  private[concurrent] val _tasks = Collections.newSetFromMap(new ConcurrentHashMap[Task[_], java.lang.Boolean])

  def tasks = _tasks

  val queued = new UnitProcessor[Task[_]]("queued")
  val started = new UnitProcessor[Task[_]]("started")
  val status = new UnitProcessor[(Task[_], Status)]("status")
  val completed = new UnitProcessor[(Task[_], Any)]("completed")
  val finished = new UnitProcessor[Task[_]]("finished")

  def executionContext: ExecutionContext
}

case class Status(message: String, progress: Double)