package org.powerscala.hierarchy

import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Element[P] extends MutableChildLike[P] with Listenable {
  def parent: P = hierarchicalParent
}
