package org.powerscala.workflow

import org.powerscala.{Finishable, Updatable}


/**
 * Workflow processes child WorkflowItems either synchronously or asynchronously until completion.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class Workflow(val items: List[WorkflowItem]) extends WorkflowItem with Updatable with Finishable {
  protected var currentItems = items
  protected var current: WorkflowItem = _

  private var first = true

  override final def update(delta: Double) = {
    super.update(delta)
    if (first) {
      begin()
      first = false
    }
    finished = act(delta.toFloat)
    if (finished) {
      end()
      first = true
    }
  }

  override def begin() = {
    super.begin()
    currentItems = items
    current = null
  }

  def act(delta: Double) = {
    if (current == null && !currentItems.isEmpty) {
      current = currentItems.head
      current.begin()
      currentItems = currentItems.tail
    }
    if (current != null) {
      if (current.act(delta) || current.finished) {
        current.end()
        current = null
      }
      false
    } else {
      true
    }
  }

  def isFinished = finished
}

object Workflow {
  def apply(workflowItems: WorkflowItem*) = new Workflow(workflowItems.toList)
}