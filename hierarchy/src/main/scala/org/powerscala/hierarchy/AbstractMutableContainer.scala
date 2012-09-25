package org.powerscala.hierarchy

import event.{ChildRemovedEvent, ChildAddedEvent}
import collection.mutable.ListBuffer
import annotation.tailrec

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class AbstractMutableContainer[T <: Element] extends Container[T] {
  protected val buffer = new ListBuffer[T]

  def contents: Seq[T] = buffer

  protected def addChild(child: T) = synchronized {
    if (child == null) {
      throw new NullPointerException("Adding a null child is not allowed")
    }

    buffer += child

    child match {
      case element: Element => Element.assignParent(element, this)
      case _ =>
    }

    fire(new ChildAddedEvent(this, child))
  }

  protected def insertChildren(index: Int, children: T*) = synchronized {
    buffer.insert(index, children: _*)
    children.foreach {
      case child => {
        if (child == null) {
          throw new NullPointerException("Adding a null child is not allowed")
        }
        child match {
          case element: Element => Element.assignParent(element, this)
          case _ =>
        }
        fire(new ChildAddedEvent(this, child))
      }
    }
  }

  protected def removeChild(child: T) = synchronized {
    fire(new ChildRemovedEvent(this, child))    // Fire before so index and hierarchy remains during the event

    buffer -= child

    child match {
      case element: Element => Element.assignParent(element, null)
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