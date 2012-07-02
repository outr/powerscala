package org.powerscala.scene

import event.{ChildRemovedEvent, ChildAddedEvent}
import org.powerscala.hierarchy.Element
import collection.mutable.ListBuffer
import annotation.tailrec

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class AbstractMutableContainer[T <: Element] extends Container[T] {
  protected val buffer = new ListBuffer[T]

  def contents: Seq[T] = buffer

  protected def addChild(child: T) = synchronized {
    buffer += child

    child match {
      case element: Element => Element.assignParent(element, this)
      case _ =>
    }

    fire(new ChildAddedEvent(this, child))
  }

  protected def removeChild(child: T) = synchronized {
    buffer -= child

    child match {
      case element: Element => Element.assignParent(element, null)
      case _ =>
    }

    fire(new ChildRemovedEvent(this, child))
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