package org.powerscala.hierarchy

import org.powerscala.hierarchy.event._
import annotation.tailrec
import org.powerscala.event.{ListenMode, Listenable}
import org.powerscala.hierarchy.event.ChildAddedEvent
import org.powerscala.hierarchy.event.ChildRemovedEvent

/**
 * ContainerView represents a flat view of the hierarchical elements of a container. The view should represent the
 * current flat list of the referenced container at all times.
 *
 * The query function optionally defines a mechanism of excluding elements from the view.
 *
 * The sort function optionally defines the sort order for elements retrieved from this view.
 *
 * The filter function optionally defines a temporary filtering of the view. This differs from the query method as it is
 * re-validated against currently included and excluded items per change and on-demand via the refreshFilter() method.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class ContainerView[T](val container: Container[_],
                          query: T => Boolean = null,
                          sort: (T, T) => Int = null,
                          filterIn: T => Boolean = null,
                          dynamic: Boolean = true)(implicit manifest: Manifest[T]) extends Iterable[T] with Listenable {
  private lazy val ordering = new Ordering[T] {
    def compare(x: T, y: T) = if (sort != null) sort(x, y) else 0
  }
  private var queue = List.empty[T]
  private var filtered = List.empty[T]

  val childAdded = new ChildAddedProcessor
  val childRemoved = new ChildRemovedProcessor

  refresh()

  // Add listeners if dynamic is enabled
  if (dynamic) {
    container.childAdded.listen(ListenMode.Standard, Descendants) {
      case added => synchronized {
        validateChild(added.child.asInstanceOf[AnyRef])
        refreshFilter()
        refreshSort()
      }
    }
    container.childRemoved.listen(ListenMode.Standard, Descendants) {
      case removed => synchronized {
        invalidateChild(removed.child.asInstanceOf[AnyRef])
        refreshFilter()
        refreshSort()
      }
    }
  }

  /**
   * Applies all items in the view to the supplied function and continues to apply against the function as new items are
   * added in the future.
   */
  def live(f: T => Unit) = {
    // Apply to everything now
    foreach(f)
    // Apply to everything going forward

    childAdded.on {
      case evt => f(evt.child.asInstanceOf[T])
    }
  }

  /**
   * Refreshes the ContainerView. This is much less efficient than allowing events to update the view automatically when
   * dynamic is set to true.
   */
  def refresh() = synchronized {
    queue = Nil
    filtered = Nil
    validateRecursive(container.contents)
    refreshFilter()
    refreshSort()
  }

  /**
   * Refreshes the filtering on this view. Validates the items currently filtered out for inclusion and validates the
   * items currently included for filtering.
   */
  def refreshFilter() = synchronized {
    if (filterIn != null) {
      validateExcluded()
      validateIncluded()
    }
  }

  /**
   * Refreshes the sorting on this view.
   */
  def refreshSort() = synchronized {
    if (sort != null) {
      queue = queue.sorted(ordering)
    }
  }

  def iterator = synchronized {
    queue.iterator
  }

  /**
   * Recursively iterates over children adding them to the queue.
   */
  @tailrec
  private def validateRecursive(children: Seq[_]): Unit = {
    if (children.nonEmpty) {
      val child = children.head
      validateChild(child.asInstanceOf[AnyRef])
      validateRecursive(children.tail)
    }
  }

  /**
   * Recursively iterates over chlidren removing them from the queue.
   */
  @tailrec
  private def invalidateRecursive(children: Seq[_]): Unit = {
    if (children.nonEmpty) {
      val child = children.head
      invalidateChild(child.asInstanceOf[AnyRef])
      invalidateRecursive(children.tail)
    }
  }

  /**
   * Validates the addition of a child.
   */
  private def validateChild(child: AnyRef) = {
    if (isValid(child)) {
      val c = child.asInstanceOf[T]
      if (filterIn == null || filterIn(c)) {
        queue = (c :: queue.reverse).reverse
        childAdded.fire(ChildAddedEvent(container, c))
      } else {
        filtered = c :: filtered
      }
    }

    child match {   // If the child is a container, we need to add its children as well
      case container: Container[_] => validateRecursive(container.contents)
      case _ =>
    }
  }

  /**
   * Removes the supplied child from the queue.
   */
  private def invalidateChild(child: AnyRef) = {
    val f = (c: T) => c == child
    if (queue.contains(child)) {
      queue = queue.filterNot(f)
      childRemoved.fire(ChildRemovedEvent(container, child))
    } else {
      filtered = filtered.filterNot(f)
    }

    child match {   // If the child is a container, we need to remove its children as well
      case container: Container[_] => invalidateRecursive(container.contents)
      case _ =>
    }
  }

  /**
   * Returns true if Manifest is assignable and query returns true.
   */
  private def isValid(child: AnyRef) = {
    manifest.runtimeClass.isAssignableFrom(child.getClass) &&      // Make sure it is of the right type
    (query == null || query(child.asInstanceOf[T])) &&        // Validate it against the query
    (!queue.contains(child) && !filtered.contains(child))     // Make sure it is not already in the system
  }

  /**
   * Validates the currently filtered items for inclusion back into the queue.
   */
  @tailrec
  private def validateExcluded(excluded: List[T] = filtered): Unit = {
    if (excluded.nonEmpty) {
      val item = excluded.head
      if (filterIn(item)) {     // Include it back into the queue
        queue = item :: queue
        filtered = filtered.filterNot(f => f == item)
        childAdded.fire(ChildAddedEvent(container, item))
      }
      validateExcluded(excluded.tail)
    }
  }

  /**
   * Validates the current included items for exclusion from the queue.
   */
  @tailrec
  private def validateIncluded(included: List[T] = queue): Unit = {
    if (included.nonEmpty) {
      val item = included.head
      if (!filterIn(item)) {      // Exclude it from the queue
        queue = queue.filterNot(i => i == item)
        filtered = item :: filtered
        childRemoved.fire(ChildRemovedEvent(container, item))
      }
      validateIncluded(included.tail)
    }
  }
}
