package org.powerscala

import annotation.tailrec
import collection.mutable.ListBuffer

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Hierarchical {
  protected def hierarchicalParents: Seq[Hierarchical]
  protected def hierarchicalChildren: Seq[Hierarchical]
  protected def hierarchicalParent = hierarchicalParents.headOption

  protected def processHierarchically(f: Hierarchical => Unit) = {
    processInternal(f, hierarchicalChildren)
  }

  @tailrec
  private def processInternal(f: Hierarchical => Unit, children: Seq[Hierarchical]): Unit = {
    if (children.nonEmpty) {
      val child = children.head
      f(child)
      child.processHierarchically(f)

      processInternal(f, children.tail)
    }
  }

  protected def hierarchicalList = {
    val b = ListBuffer.empty[Hierarchical]
    processHierarchically {
      case h => b += h
    }
    b.toList
  }
}
