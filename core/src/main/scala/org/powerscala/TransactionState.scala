package org.powerscala

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait TransactionState {
  def commit(): Unit

  def rollback(): Unit
}
