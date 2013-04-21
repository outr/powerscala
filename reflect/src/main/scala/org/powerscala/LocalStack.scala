package org.powerscala

import scala.collection.mutable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class LocalStack[T] {
  private val threadLocal = new ThreadLocal[mutable.Stack[T]] {
    override def initialValue() = new mutable.Stack[T]()
  }

  def isEmpty = threadLocal.get().isEmpty
  def nonEmpty = threadLocal.get().nonEmpty

  def apply() = threadLocal.get().top

  def get() = {
    val stack = threadLocal.get()
    if (stack.nonEmpty) {
      Some(stack.top)
    } else {
      None
    }
  }

  def context[R](value: T)(f: => R): R = {
    val stack = threadLocal.get()
    stack.push(value)
    try {
      f
    } finally {
      if (stack.top == value) {
        stack.pop()
      } else {
        throw new RuntimeException(s"Trying to remove out of order. Attempting to remove $value, but ${stack.top} is at the top of the stack!")
      }
    }
  }
}
