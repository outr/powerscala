package org.powerscala.transactional

import scala.collection.immutable.ListSet

/**
 * @author Matt Hicks <matt@outr.com>
 */
class TransactionState private[transactional]() {
  private[transactional] var actions = ListSet.empty[TransactionAction]

  def actionById[T <: TransactionAction](id: String) = actions.find(ta => ta.id == id).asInstanceOf[Option[T]]
}