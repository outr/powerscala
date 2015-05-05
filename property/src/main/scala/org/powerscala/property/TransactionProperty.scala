package org.powerscala.property

import org.powerscala.Unique
import org.powerscala.transactional.{TransactionAction, transaction}

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TransactionProperty[T] extends Property[T] {
  private val transactionId = Unique()

  override def apply(): T = transaction.actionById[TransactionChange](transactionId) match {
    case Some(tc) => tc.newValue
    case None => super.apply()
  }

  override def apply(value: T, handling: EventHandling) = transaction.action(TransactionChange(apply(), value, handling))

  case class TransactionChange(oldValue: T, newValue: T, handling: EventHandling) extends TransactionAction {
    override def id = transactionId

    override def localized = true

    override def commit() = TransactionProperty.super.apply(newValue, handling)

    override def rollback() = {}

    override def toString = s"TransactionChange($id, $oldValue, $newValue)"
  }
}