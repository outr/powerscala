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

  override def apply(value: T, handling: EventHandling) = if (transaction.active) {
    transaction.set(this, TransactionChange(apply(), value, handling))
  } else {
    super.apply(value, handling)
  }

  case class TransactionChange(oldValue: T, newValue: T, handling: EventHandling) extends TransactionState {
    override def commit() = apply(newValue, handling)

    override def rollback() = {}
  }
}