package org.powerscala.transactional

import org.powerscala.{LocalStack, Unique}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object transaction {
  private val states = new LocalStack[TransactionState]

  def apply[R](f: => R): R = {
    begin()
    try {
      val result: R = f
      commit()
      result
    } catch {
      case t: Throwable => {
        rollback()
        throw t
      }
    }
  }

  def begin() = {
    val state = new TransactionState
    states.push(state)
  }

  def commit() = try {
    states.pop().actions.toList.foreach(ta => ta.commit())
  } catch {
    case t: Throwable => t.printStackTrace()
  }

  def rollback() = {
    states.pop().actions.toList.foreach(ta => ta.rollback())
  }

  def action(action: TransactionAction, errorIfNoTransaction: Boolean = false) = state(action.localized) match {
    case Some(s) => {
      if (s.actions.contains(action)) s.actions -= action   // ListSet doesn't replace if equality checks
      s.actions += action
    }
    case None if errorIfNoTransaction => throw new NoTransactionException(s"No transaction on current thread when attempting to add action: $action.")
    case None => action.commit()
  }

  def onCommit(id: String = Unique(), errorIfNoTransaction: Boolean = false, localized: Boolean = false)(f: => Unit) = {
    val customId = id
    val customLocalized = localized
    val action = new TransactionAction {
      override def id = customId

      override def localized = customLocalized

      override def rollback() = {}

      override def commit() = f
    }
    this.action(action, errorIfNoTransaction)
  }

  def onRollback(id: String = Unique(), errorIfNoTransaction: Boolean = false, localized: Boolean = false)(f: => Unit) = {
    val customId = id
    val customLocalized = localized
    val action = new TransactionAction {
      override def id = customId

      override def localized = customLocalized

      override def rollback() = f

      override def commit() = {}
    }
    this.action(action, errorIfNoTransaction)
  }

  def rootState = states.lastOption

  def currentState = states.get()

  def state(localized: Boolean) = if (localized) currentState else rootState

  def actionById[T <: TransactionAction](id: String) = currentState.map(ts => ts.actionById[T](id)).flatten.orElse(rootState.map(ts => ts.actionById[T](id)).flatten)
}

class NoTransactionException(message: String) extends RuntimeException(message)