package org.powerscala.command

trait CommandManager {
  def implementation: CommandImplementation
  def interpreter: CommandInterpreter

  def process(command: Command): Unit
  def send(command: Command): Unit = {
    val line = interpreter.fromCommand(command)
    implementation.send(line)
  }

  implementation.start(this)
}