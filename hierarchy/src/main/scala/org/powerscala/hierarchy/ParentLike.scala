package org.powerscala.hierarchy

/**
 * ParentLike defines the high-level concept of an instance that has associated children.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait ParentLike {
  protected def hierarchicalChildren: Seq[Any]
}

object ParentLike {
  def childrenOf(parent: ParentLike) = parent.hierarchicalChildren

  def selfAndDescendants(entry: Any): Iterator[Any] = entry match {
    case parent: ParentLike => Iterator(entry) ++ parent.hierarchicalChildren.iterator.flatMap(selfAndDescendants _)
    case _ => Iterator(entry)
  }

  def descendants(entry: Any): Iterator[Any] = entry match {
    case parent: ParentLike => parent.hierarchicalChildren.iterator.flatMap(selfAndDescendants _)
    case _ => Iterator.empty
  }
}
