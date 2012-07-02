package org.powerscala.workflow.item

import org.powerscala.workflow.WorkflowItem

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class WaitFor(condition: () => Boolean) extends WorkflowItem {
  def act(delta: Double) = condition()
}
