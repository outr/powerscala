package org.powerscala.command

/**
  * CommandImplementation handles the method of communication.
  */
trait CommandImplementation {
  def start(manager: CommandManager): Unit

  def send(line: String): Unit

  def dispose(): Unit
}
