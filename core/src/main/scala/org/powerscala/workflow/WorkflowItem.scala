package org.powerscala.workflow

/**
 * WorkflowItem is the core class that all aspects of Workflow must extend from.
 *
 * A WorkflowItem defines a begin, act, and end.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait WorkflowItem {
  protected[workflow] var id: String = _
  protected[workflow] var finished = false

  /**
   * Called when this WorkflowItem is beginning.
   */
  def begin() = {
    finished = false
    if (id != null) {
      WorkflowItem.register(id, this)
    }
  }

  /**
   * Acts on the workflow item returning true if it's completed.
   */
  def act(delta: Double): Boolean

  /**
   * Called when this WorkflowItem is ending.
   */
  def end() = {
    if (id != null) {
      WorkflowItem.unregister(id)
    }
  }

  /**
   * Called explicitly to stop this workflow item if it is running.
   */
  def stop() = {
    finished = true
  }
}

object WorkflowItem {
  private var items = Map.empty[String, WorkflowItem]

  private def register(id: String, item: WorkflowItem) = synchronized {
    items += id -> item
  }

  private def unregister(id: String) = synchronized {
    items -= id
  }

  /**
   * Look up a currently executing WorkflowItem by id.
   */
  def apply(id: String) = items(id)
}