package org.powerscala.property.workflow

import org.powerscala.property.MutableProperty
import org.powerscala.workflow.WorkflowBuilder
import org.powerscala.easing.Easing

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class PropertyAnimatorBuilder(builder: WorkflowBuilder) {
  def and(property: MutableProperty[Double]) = {
    builder.add(PropertyAnimator(property, Double.NegativeInfinity, Double.NegativeInfinity))
  }

  def moveTo(destination: Double) = {
    val items = builder.currentItems.map {
      case item: PropertyAnimator if (item.destination == Double.NegativeInfinity) => item.copy(destination = destination)
      case item => item
    }
    builder.copy(currentItems = items)
  }

  def in(time: Double) = {
    val items = builder.currentItems.map {
      case item: PropertyAnimator if (item.time == Double.NegativeInfinity) => item.copy(time = time)
      case item => item
    }
    builder.copy(currentItems = items)
  }

  def using(easing: Easing) = {
    val items = builder.currentItems.map {
      case item: PropertyAnimator if (item.easing == null) => item.copy(easing = easing)
      case item => item
    }
    builder.copy(currentItems = items)
  }
}
