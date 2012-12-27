package org.powerscala

import collection.mutable.ListBuffer

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait MutableHierarchical extends Hierarchical {
  private val _parents = ListBuffer.empty[Hierarchical]
  private val _children = ListBuffer.empty[Hierarchical]

  protected def hierarchicalParents = _parents
  protected def hierarchicalChildren = _children

  protected def modifyHierarchicalParents[T](f: ListBuffer[Hierarchical] => T) = synchronized {
    f(_parents)
  }

  protected def modifyHierarchicalChildren[T](f: ListBuffer[Hierarchical] => T) = synchronized {
    f(_children)
  }

  protected def addParent(p: MutableHierarchical): Boolean = synchronized {
    if (!hierarchicalParents.contains(p)) {
      _parents += p
      p.addChild(this)
      parentAdded(p)
      true
    } else {
      false
    }
  }

  protected def addChild(c: MutableHierarchical): Boolean = synchronized {
    if (!hierarchicalChildren.contains(c)) {
      _children += c
      c.addParent(this)
      childAdded(c)
      true
    } else {
      false
    }
  }

  protected def parentAdded(parent: Hierarchical) = {}

  protected def childAdded(child: Hierarchical) = {}
}