package org.powerscala

import scala.collection.mutable

/**
 * @author Matt Hicks <matt@outr.com>
 */
object transaction {
  private val _states = new LocalStack[mutable.Map[Any, TransactionState]]

  def apply[R](f: => R): R = {
    begin()
    try {
      val result = f
      commit()
      result
    } catch {
      case t: Throwable => {
        rollback()
        throw t
      }
    } finally {
      end()
    }
  }

  def active = _states.nonEmpty

  def begin() = {
    _states.push(mutable.Map.empty)             // Add a new state map
  }

  def commit() = {
    val state = end()                             // Remove and reference the current state
    try {
      state.values.foreach(ts => ts.commit())     // Iterate over the values and commit them
    } finally {
      begin()                                     // Create a new state
    }
  }

  def rollback() = {
    val state = end()                             // Remove and reference the current state
    try {
      state.values.foreach(ts => ts.rollback())   // Iterate over the values and roll them back
    } finally {
      begin()                                     // Create a new state
    }
  }

  def end() = _states.pop()     // Remove the current state map

  def state[T <: TransactionState](ref: Any): Option[T] = _states.stack.collectFirst {
    case map if map.contains(ref) => map(ref).asInstanceOf[T]
  }

  def set[T <: TransactionState](ref: Any, state: T) = if (_states.isEmpty) {
    state.commit()
  } else {
    _states() += ref -> state
  }
}
