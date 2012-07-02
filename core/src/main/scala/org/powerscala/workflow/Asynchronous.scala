package org.powerscala.workflow

import annotation.tailrec

/**
 * Asynchronous trait can be mixed into a Workflow to invoke all child workflow items simultaneously
 * completing only when all child workflow items have completed.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Asynchronous extends Workflow {
  override def begin() = {
    super.begin()

    currentItems.foreach(Asynchronous.beginItem)
  }

  override def act(delta: Double) = {
    updateAsync(delta, currentItems)
    currentItems.isEmpty
  }

  @tailrec
  private def updateAsync(delta: Double, items: List[WorkflowItem]): Unit = {
    if (!items.isEmpty) {
      val item = items.head
      if (item.act(delta) || item.finished) {
        item.end()
        currentItems = currentItems.filterNot(wi => wi == item)
      }
      updateAsync(delta, items.tail)
    }
  }
}

object Asynchronous {
  private val beginItem = (item: WorkflowItem) => item.begin()
}