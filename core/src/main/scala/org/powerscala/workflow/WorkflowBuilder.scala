package org.powerscala.workflow

import item.Delay


/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
case class WorkflowBuilder(currentItems: List[WorkflowItem] = Nil,
                           workflowItems: List[WorkflowItem] = Nil,
                           loopCount: Int = 0) {
  def pause(time: Double) = nextStep().copy(currentItems = List(Delay(time))).nextStep()

  def delay(time: Double) = pause(time)

  def loop(count: Int) = copy(loopCount = count)

  def then(item: WorkflowItem) = nextStep().add(item)

  def add(item: WorkflowItem) = copy(currentItems = item :: currentItems)

  def repeat(r: Repeat) = {
    val b = nextStep()
    r match {
      case Repeat.All => b.copy(workflowItems = b.workflowItems ::: b.workflowItems)
      case Repeat.First => b.copy(workflowItems = b.workflowItems.last :: b.workflowItems)
      case Repeat.Last => b.copy(workflowItems = b.workflowItems.head :: b.workflowItems)
    }
  }

  def nextStep() = {
    val items = if (currentItems.nonEmpty) {
      AsynchronousWorkflow(currentItems) :: workflowItems
    } else {
      workflowItems
    }
    copy(currentItems = Nil, workflowItems = items)
  }

  def workflow = {
    val builder = nextStep()
    new Workflow(builder.workflowItems.reverse) with Looping {
      val loops = loopCount
    }
  }

  override def toString = {
    val b = new StringBuilder("WorkflowBuilder(\r\n")
    b.append("\tcurrentItems:\r\n")
    currentItems.foreach {
      case item => b.append("\t\t%s\r\n".format(item))
    }
    b.append("\tworkflowItems:\r\n")
    workflowItems.foreach {
      case item => b.append("\t\t%s\r\n".format(item))
    }
    b.append("\tloopCount: %s\r\n".format(loopCount))
    b.append(')')
    b.toString()
  }
}

case class AsynchronousWorkflow(workflowItems: List[WorkflowItem]) extends Workflow(workflowItems) with Asynchronous