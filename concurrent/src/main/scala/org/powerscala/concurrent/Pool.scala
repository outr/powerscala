package org.powerscala.concurrent

import java.util.concurrent.ConcurrentLinkedQueue

import scala.language.postfixOps

/**
 * Pool offers a fairly simplistic object pooling implementation.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait Pool[T] {
  private val queue = new ConcurrentLinkedQueue[T]()
  private val items = new AtomicInt(0)

  init()

  private def init() = {
    if (initialSize > maximumSize) {
      throw new IndexOutOfBoundsException(s"Initial size ($initialSize) cannot be larger than maximum size ($maximumSize)")
    }
    (0 until initialSize).foreach {
      case index => {
        queue.add(createItem())
        items++
      }
    }
  }

  /**
   * Requests an item from the pool for use. Will block for <code>waitTime</code> for the availability of the item.
   *
   * @param waitTime the amount of time in seconds to wait for an available item from the pool. Defaults to
   *                 Double.MaxValue.
   * @param create if true an item will be added to the queue if there are no items available and the pool isn't fully
   *               utilized. Defaults to true.
   * @return Some[T] if the pool is not fully utilized
   */
  def request(waitTime: Double = Double.MaxValue, create: Boolean = true): Option[T] = {
    val item = queue.poll()
    if (item == null) {
      if (create && items.incrementIfLessThan(maximumSize)) {   // Create a new item to return
        Some(createItem())
      } else {
        var waiting: Option[T] = None
        Time.waitFor(waitTime) {
          waiting = Option(queue.poll())
          if (waiting.nonEmpty) {
            true
          } else if (create && items.incrementIfLessThan(maximumSize)) {
            waiting = Some(createItem())
            true
          } else {
            false
          }
        }
        waiting
      }
    } else {
      Some(item)
    }
  }

  /**
   * Waits perpetually until an item is available in the pool and then returns it.
   *
   * @return T
   */
  def apply() = request().get

  /**
   * Releases the item back to the pool for re-use. Calls <code>releaseItem(t)</code> before it is returned to the
   * queue.
   *
   * @param t the item to return.
   */
  def release(t: T): Unit = {
    releaseItem(t)
    queue.add(t)
  }

  /**
   * Disposes the item and does not return it to the pool. This lets the pool know that a new item can be created to
   * replace it. Calls <code>disposeItem(t)</code> before the item count is reduced to allow any cleanup necessary to
   * occur.
   *
   * @param t the item to dispose.
   */
  def dispose(t: T): Unit = {
    disposeItem(t)
    items--     // Remove from item count
  }

  /**
   * Use allows safe usage of an item with a guaranteed release back to the pool. The supplied function will receive an
   * an item when one becomes available (blocking) within <code>waitTime</code>. If <code>waitTime</code> elapses before
   * an item becomes available then the function will not be invoked an <code>None</code> will be returned.
   *
   * @param waitTime the amount of time to wait for an item to become available in the pool. Defaults to Double.MaxValue
   * @param create if true an item will be added to the queue if there are no items available and the pool isn't fully
   *               utilized. Defaults to true.
   * @param f the function to invoke with the item.
   * @tparam R the return type of the function
   * @return Some[R] only if the function was able to successfully execute within the <code>waitTime</code>.
   */
  def use[R](waitTime: Double = Double.MaxValue, create: Boolean = true)(f: T => R): Option[R] = {
    request(waitTime, create = create) match {
      case Some(item) => try {
        Some(f(item))
      } finally {
        release(item)
      }
      case None => None
    }
  }

  /**
   * The initial number of items that should be created in the pool.
   */
  def initialSize: Int

  /**
   * The maximum number of items that can exist in the pool.
   */
  def maximumSize: Int

  /**
   * Creates an item for use in the pool. This method is invoked internally and should never be externally exposed.
   */
  protected def createItem(): T

  /**
   * Called immediately before an item is placed back into the pool. There is no requirement for this method to do
   * anything, but allows for cleanup before the item is recycled into the pool.
   *
   * @param t the item being released.
   */
  protected def releaseItem(t: T): Unit

  /**
   * Called immediately before an item is disposed from the pool. There is no requirement for this method to do
   * anything, but allows for cleanup before this item is dumped from the pool.
   *
   * @param t the item being disposed.
   */
  protected def disposeItem(t: T): Unit
}

object Pool {
  def apply[T](f: => T, initialSize: Int = 0, maximumSize: Int = Int.MaxValue) = {
    new SimplePool[T](initialSize, maximumSize, f)
  }
}

/**
 * SimplePool offers defaults for most of Pool values and only really needs an implementation of create.
 *
 * @param initialSize the initial size, defaults to 0.
 * @param maximumSize the maximum size, defaults to Int.MaxValue.
 * @tparam T the item type in the pool.
 */
class SimplePool[T](val initialSize: Int = 0, val maximumSize: Int = Int.MaxValue, f: => T) extends Pool[T] {
  protected def createItem() = f

  protected def releaseItem(t: T) {}
  protected def disposeItem(t: T) {}
}
