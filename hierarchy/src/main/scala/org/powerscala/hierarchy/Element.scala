package org.powerscala.hierarchy

import org.powerscala.event.Listenable
import org.powerscala.TypeFilteredIterator

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Element[P] extends MutableChildLike[P] with Listenable {
  def parent: P = hierarchicalParent
  def root[T](implicit manifest: Manifest[T]) = TypeFilteredIterator[T](ChildLike.selfAndAncestors(this)).toStream.lastOption
}
