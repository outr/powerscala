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
  def stack = threadLocal.get()

  def head = threadLocal.get().head

  def headOption = threadLocal.get().headOption

  def last = threadLocal.get().last

  def lastOption = threadLocal.get().lastOption

  def length = threadLocal.get().length

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

  def push(value: T) = {
    val stack = threadLocal.get()
    stack.push(value)
  }

  def pop(): T = {
    val stack = threadLocal.get()
    stack.pop()
  }

  def pop(value: T): T = {
    val current = threadLocal.get().head
    if (current != value) {
      throw new RuntimeException(s"Expecting to pop: $value, but current stack contains: $current.")
    }
    pop()
  }

  def clear() = {
    val stack = threadLocal.get()
    stack.clear()
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
    push(value)
    try {
      f
    } finally {
      pop()
    }
  }
}
