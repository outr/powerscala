package org.powerscala.hierarchy

/**
 * ChildLike defines the high-level concept of an instance that has an associated parent.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait ChildLike {
  protected def hierarchicalParent: Any
}

object ChildLike {
  def parentOf(child: ChildLike) = child.hierarchicalParent

  def selfAndAncestors(entry: Any): Iterator[Any] = entry match {
    case child: ChildLike => Iterator(entry) ++ selfAndAncestors(child.hierarchicalParent)
    case _ => Iterator.empty
  }

  def ancestors(entry: Any): Iterator[Any] = entry match {
    case child: ChildLike => selfAndAncestors(child.hierarchicalParent)
  }
}