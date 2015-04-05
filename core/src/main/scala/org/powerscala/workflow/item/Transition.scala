package org.powerscala.workflow.item

import org.powerscala.workflow.WorkflowItem

/**
 * Transitions over the <code>time</code> specified calling <code>handler</code> as time progresses with the percentage
 * complete between 0.0 and 1.0.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class Transition(time: Double)(handler: Double => Unit) extends WorkflowItem {
  private var elapsed: Double = _

  override def begin() = {
    super.begin()
    elapsed = 0.0
  }

  override def act(delta: Double) = {
    elapsed += delta
    val percent = math.min(1.0, elapsed / time)
    handler(percent)
    elapsed >= time
  }

  override def end() = {
    super.end()
    elapsed = time
  }
}
