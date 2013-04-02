package org.powerscala

import workflow.item.{WaitFor, InvokeFunction}

package object workflow {
  import language.implicitConversions

  val all = Repeat.All
  val first = Repeat.First
  val last = Repeat.Last

  implicit def builder2workflow(builder: WorkflowBuilder) = builder.workflow

  def invoke(f: => Any) = InvokeFunction(() => f)

  def waitFor(condition: => Boolean) = WaitFor(() => condition)
}