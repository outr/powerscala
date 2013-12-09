package org.powerscala.process

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ProcessState private() extends EnumEntry

object ProcessState extends Enumerated[ProcessState] {
  val NotStarted = new ProcessState
  val Starting = new ProcessState
  val Running = new ProcessState
  val Finished = new ProcessState
  val Error = new ProcessState
}