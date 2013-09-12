package org.powerscala

import scala.collection.mutable

/**
 * LocalStack is backed by a ThreadLocal stack of T.
 *
 * @author Matt Hicks <matt@outr.com>
 */
class LocalStack[T] {
  private val threadLocal = new ThreadLocal[mutable.Stack[T]] {
    override def initialValue() = new mutable.Stack[T]()
  }

  def isEmpty = threadLocal.get().isEmpty
  def nonEmpty = threadLocal.get().nonEmpty

  /**
   * Returns the top of the stack. Will throw an exception if the stack is empty.
   */
  def apply() = threadLocal.get().top

  /**
   * Returns Some(t) or None if the stack is empty.
   */
  def get() = {
    val stack = threadLocal.get()
    if (stack.nonEmpty) {
      Some(stack.top)
    } else {
      None
    }
  }

  /**
   * Pushes value onto the stack only for the duration of f.
   *
   * @param value the value to push onto the stack
   * @param f the function to execute with value on the stack
   * @tparam R the result of the function
   * @return R
   */
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
