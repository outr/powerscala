package org.powerscala.collection

import scala.annotation.tailrec
import scala.collection.mutable

/**
  * HierarchicalIterator, as the name suggests, allows you to flatly iterate over a hierarchical structure.
  *
  * @param root the root element to begin iterating with
  * @param childrenIterator a function that takes in `T` and returns an `Iterator` of its children.
  * @tparam T the type to be iterated over
  */
class HierarchicalIterator[T](root: T, childrenIterator: T => Iterator[T]) extends Iterator[T] {
  private val stack = mutable.Stack[Iterator[T]](Iterator.single(root))

  override def hasNext: Boolean = stack.exists(_.hasNext)

  @tailrec
  override final def next(): T = {
    val iterator = stack.head
    if (iterator.hasNext) {
      val t = iterator.next()
      stack.push(childrenIterator(t))
      t
    } else {
      stack.pop()
      next()
    }
  }
}