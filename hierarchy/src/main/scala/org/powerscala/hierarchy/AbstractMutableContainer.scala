package org.powerscala.hierarchy

import org.powerscala.hierarchy.event.{ChildRemovedEvent, ChildAddedEvent}
import collection.mutable.ListBuffer
import annotation.tailrec

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class AbstractMutableContainer[E] extends Container[E] {
  protected val buffer = new ListBuffer[E]

  def contents: Seq[E] = buffer

  protected def addChild(child: E) = synchronized {
    if (child == null) {
      throw new NullPointerException("Adding a null child is not allowed")
    }

    buffer += child

    child match {
      case mcl: MutableChildLike[_] => MutableChildLike.assignParent(mcl, this)
      case _ =>
    }

    childAdded.fire(ChildAddedEvent(this, child))
  }

  protected def insertChildren(index: Int, children: E*) = synchronized {
    buffer.insert(index, children: _*)
    children.foreach {
      case child => {
        if (child == null) {
          throw new NullPointerException("Adding a null child is not allowed")
        }
        child match {
          case mcl: MutableChildLike[_] => MutableChildLike.assignParent(mcl, this)
          case _ =>
        }
        childAdded.fire(ChildAddedEvent(this, child))
      }
    }
  }

  protected def removeChild(child: E) = synchronized {
    childRemoved.fire(ChildRemovedEvent(this, child)) // Fire before so index and hierarchy remains during the event

    buffer -= child

    child match {
      case mcl: MutableChildLike[_] => MutableChildLike.assignParent(mcl, null)
      case _ =>
    }
  }

  protected def removeFirst() = synchronized {
    if (buffer.nonEmpty) {
      val child = buffer.head
      removeChild(child)
      true
    } else {
      false
    }
  }

  @tailrec
  protected final def removeAll(): Unit = {
    if (removeFirst()) {
      removeAll()
    }
  }
}