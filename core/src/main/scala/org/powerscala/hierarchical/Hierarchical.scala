package org.powerscala.hierarchical

import scala.collection.immutable.Stack
import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Hierarchical {
}

trait ParentLike extends Hierarchical {
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

trait ChildLike extends Hierarchical {
  protected def hierarchicalParent: Any
}

object ChildLike {
  def parentOf(child: ChildLike) = child.hierarchicalParent
}