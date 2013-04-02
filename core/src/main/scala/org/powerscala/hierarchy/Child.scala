package org.powerscala.hierarchy

import org.powerscala.reflect._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Child {
  def parent: Any

  /**
   * Walks up the hierarchical structure returning Some[T] if the top-level element is of type T. Otherwise None is
   * returned. Note that this method may return itself if it has no parent and matches the conditions of T.
   */
  def root[T](implicit manifest: Manifest[T]): Option[T] = parent match {
    case child: Child => child.root[T](manifest)
    case null => if (getClass.hasType(manifest.runtimeClass)) {
      Some(this.asInstanceOf[T])
    } else {
      None
    }
    case p if (p.asInstanceOf[AnyRef].getClass.hasType(manifest.runtimeClass)) => Some(p.asInstanceOf[T])
    case _ => None
  }
}

object Child {
  /**
   * Returns true if the value passed is in the ancestry hierarchy for this Child.
   */
  def hasAncestor[T](child: Child, value: T, maxDepth: Int = Int.MaxValue)(implicit manifest: Manifest[T]) = ancestor(child, (t: T) => t == value, maxDepth)(manifest) != None

  /**
   * Returns true if the value passed is the parent of this Child.
   */
  def hasParent[T](child: Child, value: T)(implicit manifest: Manifest[T]) = hasAncestor(child, value, 1)(manifest)

  /**
   * Uses the supplied matching function to return the first ancestor match given the specified type or None if no
   * match is found.
   */
  def ancestor[T](child: Child, matcher: T => Boolean, maxDepth: Int = Int.MaxValue)(implicit manifest: Manifest[T]): Option[T] = {
    child.parent match {
      case p: Child if (manifest.runtimeClass.isAssignableFrom(p.asInstanceOf[AnyRef].getClass) && matcher(p.asInstanceOf[T])) => Option(p.asInstanceOf[T])
      case p: Child if (maxDepth > 1) => ancestor[T](p, matcher, maxDepth - 1)(manifest)
      case _ => None
    }
  }
}