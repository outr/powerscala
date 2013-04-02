package org.powerscala.property

import org.powerscala.workflow.WorkflowBuilder

package object workflow {
  import language.implicitConversions

  implicit def wb2pab(b: WorkflowBuilder) = new PropertyAnimatorBuilder(b)

  implicit def p2pab(p: MutableProperty[Double]) = new PropertyAnimatorBuilder(new WorkflowBuilder().and(p))

  implicit def p2pa(p: MutableProperty[Double]) = PropertyAnimator(p, Double.NegativeInfinity, Double.NegativeInfinity)
}