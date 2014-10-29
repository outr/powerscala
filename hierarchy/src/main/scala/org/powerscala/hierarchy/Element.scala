package org.powerscala.hierarchy

import org.powerscala.event.{Listener, Listenable}
import org.powerscala.{Priority, TypeFilteredIterator}
import org.powerscala.hierarchy.event.{Ancestors, ChildAddedEvent}

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Element[P] extends MutableChildLike[P] with Listenable {
  def parent: P = hierarchicalParent
  def root[T](implicit manifest: Manifest[T]) = TypeFilteredIterator[T](ChildLike.selfAndAncestors(this)).toStream.lastOption

  /**
   * Invoke the supplied function when P is an available ancestor. This will be invoked a maximum of one times and zero
   * times if P never appears as an ancestor.
   *
   * @param f the function to invoke
   * @param manifest the manifest for the generic ancestor
   * @tparam Ancestor the ancestor type to find
   */
  def connected[Ancestor](f: Ancestor => Unit)(implicit manifest: Manifest[Ancestor]) = {
    root[Ancestor] match {
      case Some(p) => f(p)      // Root of type already exists
      case None => {            // Wait for it to be hierarchically attached
        @volatile var listener: Listener[ChildAddedEvent, Unit] = null
        listener = listen[ChildAddedEvent, Unit, Unit]("childAdded", Priority.Normal, Ancestors) {
          case evt => root[Ancestor] match {
            case Some(p) => {
              listeners -= listener
              f(p)
            }
            case None => // Not connected yet
          }
        }
      }
    }
  }
}
