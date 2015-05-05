package org.powerscala.transactional

import org.powerscala.Unique

/**
 * TransactionAction is stored within a TransactionState and is persisted when the last transaction wrapper is closed or
 * rolled back when an exception occurs. The action is stored in a Set, so overriding a previous entry can be done by
 * providing an overridden equals and hash or providing a consistent id.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait TransactionAction {
  def id: String = Unique()

  /**
   * Returns true if commit and rollback should be called in a localized transaction, meaning that if you have a
   * transaction within a transaction commit or rollback will be invoked on the inner transaction instead of the
   * outermost transaction. False will always be invoked on the outermost.
   *
   * Defaults to false.
   */
  def localized: Boolean = false

  def commit(): Unit

  def rollback(): Unit

  override def toString = s"${getClass.getSimpleName}($id)"

  override def equals(o: scala.Any) = o match {
    case a: TransactionAction => a.id == id
    case _ => false
  }

  override def hashCode() = id.hashCode
}