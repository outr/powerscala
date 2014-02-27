package org.powerscala.concurrent

import java.util.concurrent.{ScheduledFuture, ConcurrentLinkedQueue}
import java.util.concurrent.atomic.AtomicLong
import scala.annotation.tailrec

/**
 * DelayedQueue allows enqueuing of items that are handled by the handler after a specific delay.
 *
 * If delay is 5.0 and quietPeriod is 2.0 items can continue to be added for three seconds before reaching the quiet
 * period when items are backlogged.
 *
 * @param delay the amount of time in seconds to delay after the first item is enqueued
 * @param quietPeriod the amount of time within the delay before the handler is called when no additional items can be
 *                    added to the queue (they get backlogged within this time period and pushed into the next batch).
 * @param handler the function to deal with the items in the queue after the delay has elapsed.
 * @tparam T the type of items this queue accepts
 */
class DelayedQueue[T](delay: Double, quietPeriod: Double, handler: T => Unit) {
  private val backlog = new ConcurrentLinkedQueue[T]()
  private val queue = new ConcurrentLinkedQueue[T]()
  private val start = new AtomicLong(0L)
  @volatile private var future: ScheduledFuture[Unit] = _

  @tailrec
  private def execute(): Unit = {
    val item = queue.poll()
    if (item == null) {
      synchronized {
        start.set(0L)
        queueBacklog()
      }
      if (!queue.isEmpty) {
        schedule()          // There are more items to process, so lets schedule another run
      }
    } else {
      handler(item)
      execute()
    }
  }

  @tailrec
  private def queueBacklog(): Unit = {
    val item = backlog.poll()
    if (item == null) {
      // Finished
    } else {
      queue.add(item)
      queueBacklog()
    }
  }

  def enqueue(item: T) = synchronized {
    val current = System.currentTimeMillis()
    val scheduled = start.compareAndSet(0L, current)
    val time = Time.fromMillis(current - start.get())
    if (time >= delay - quietPeriod) {    // Within quiet period or currently running
      backlog.add(item)
    } else {
      queue.add(item)
    }
    if (scheduled) {
      future = Executor.schedule(delay) {
        execute()
      }
    }
  }

  private def schedule() = if (start.compareAndSet(0L, System.currentTimeMillis())) {
    future = Executor.schedule(delay) {
      execute()
    }
  }
}

object DelayedQueue {
  def apply[T](delay: Double, quietPeriod: Double)(f: T => Unit) = new DelayedQueue[T](delay, quietPeriod, f)
}