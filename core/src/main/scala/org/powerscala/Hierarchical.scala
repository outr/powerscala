package org.powerscala

import collection.mutable.ListBuffer
import annotation.tailrec

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Hierarchical {
  private val _parents = ListBuffer.empty[Hierarchical]
  private val _children = ListBuffer.empty[Hierarchical]

  protected def hierarchicalParents = _parents
  protected def hierarchicalParent = _parents.headOption
  protected def hierarchicalChildren = _children

  protected def modifyHierarchicalParents[T](f: ListBuffer[Hierarchical] => T) = synchronized {
    f(_parents)
  }

  protected def modifyHierarchicalChildren[T](f: ListBuffer[Hierarchical] => T) = synchronized {
    f(_children)
  }

  protected def addParent(p: Hierarchical) = synchronized {
    if (!hierarchicalParents.contains(p)) {
      _parents += p
      p.addChild(this)
      parentAdded(p)
    }
  }

  protected def addChild(c: Hierarchical) = synchronized {
    if (!hierarchicalChildren.contains(c)) {
      _children += c
      c.addParent(this)
      childAdded(c)
    }
  }

  protected def parentAdded(parent: Hierarchical) = {}

  protected def childAdded(child: Hierarchical) = {}

  protected def processHierarchically(f: Hierarchical => Unit) = {
    processInternal(f, _children)
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