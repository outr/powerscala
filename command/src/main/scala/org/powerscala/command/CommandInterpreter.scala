package org.powerscala.command

/**
  * CommandInterpreter interprets commands that are received by an implementation.
  */
trait CommandInterpreter {
  def toCommand(line: String): Option[Command]

  def fromCommand(command: Command): String
}