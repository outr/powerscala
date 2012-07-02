package org.powerscala.workflow.item

import org.powerscala.workflow.WorkflowItem

/**
 * Invokes a function when called in the Workflow.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class InvokeFunction private(f: () => Any) extends WorkflowItem {
  def act(delta: Double) = {
    f()
    true
  }

  override def toString = "InvokeFunction(%s)".format(hashCode())
}

object InvokeFunction {
  /**
   * Creates an InvokeFunction instance that will call the supplied function upon execution.
   */
  def apply(f: () => Any) = new InvokeFunction(f)

  /**
   * Convenience method to create an InvokeFunction that will call the supplied function upon
   * execution.
   */
  def when(f: => Any) = {
    apply(() => f)
  }
}