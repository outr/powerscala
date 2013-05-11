package org.powerscala.hierarchy

/**
 * ParentLike defines the high-level concept of an instance that has associated children.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait ParentLike[C] {
  protected def hierarchicalChildren: Seq[C]
}

object ParentLike {
  def childrenOf(parent: ParentLike[_]) = parent.hierarchicalChildren

  def selfAndDescendants(entry: Any): Iterator[Any] = entry match {
    case parent: ParentLike[_] => Iterator(entry) ++ parent.hierarchicalChildren.iterator.flatMap(selfAndDescendants _)
    case _ => Iterator(entry)
  }

  def descendants(entry: Any): Iterator[Any] = entry match {
    case parent: ParentLike[_] => parent.hierarchicalChildren.iterator.flatMap(selfAndDescendants _)
    case _ => Iterator.empty
  }
}
