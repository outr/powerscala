package org.powerscala.workflow.item

import org.powerscala.workflow.WorkflowItem

/**
 * Delays before completing for the time specified.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class Delay(time: Double) extends WorkflowItem {
  private var elapsed: Double = _

  override def begin() = {
    super.begin()
    elapsed = 0.0
  }

  def act(delta: Double) = {
    elapsed += delta
    elapsed >= time
  }

  override def end() = {
    super.end()
    elapsed = time
  }
}