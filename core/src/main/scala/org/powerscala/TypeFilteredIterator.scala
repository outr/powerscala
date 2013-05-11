package org.powerscala

import scala.annotation.tailrec

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class TypeFilteredIterator[T](iterator: Iterator[_ >: T])(implicit manifest: Manifest[T]) extends Iterator[T] {
  private var current: Option[T] = None

  def hasNext = if (current.isDefined) {
    true
  } else {
    current = findNext()
    current.isDefined
  }

  def next() = if (hasNext) {
    try {
      current.get
    } finally {
      current = None
    }
  } else {
    throw new IndexOutOfBoundsException("No more items for this iterator.")
  }

  @tailrec
  private def findNext(): Option[T] = if (!iterator.hasNext) {
    None
  } else {
    val next = iterator.next()
    if (next != null && manifest.runtimeClass.isAssignableFrom(next.getClass)) {
      Some(next.asInstanceOf[T])
    } else {
      findNext()
    }
  }
}