package org.powerscala.property

import org.powerscala.transaction
import org.powerscala.TransactionState

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TransactionProperty[T] extends Property[T] {
  override def apply(): T = transaction.state[TransactionChange](this) match {
    case Some(tc) => tc.newValue
    case None => super.apply()
  }

  override def apply(value: T, suppressEvent: Boolean): Unit = if (transaction.active) {
    propertyChanging(value) match {
      case Some(newValue) if isChange(newValue) => transaction.set(this, TransactionChange(apply(), value))
      case _ => // Don't change the value
    }
  } else {
    super.apply(value, suppressEvent)
  }

  case class TransactionChange(oldValue: T, newValue: T) extends TransactionState {
    override def commit() = propertyChange(newValue)

    override def rollback() = {}
  }
}