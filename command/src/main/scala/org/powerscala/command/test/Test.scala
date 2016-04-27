package org.powerscala.command.test

import org.powerscala.command.{Command, CommandImplementation, CommandInterpreter, CommandManager, StandardIO}

object Test extends CommandManager {
  override def implementation: CommandImplementation = new StandardIO

  override def interpreter: CommandInterpreter = Interpreter

  override def process(command: Command): Unit = command match {
    case Quit => {
      println(s"Asked to quit, quitting!")
      System.exit(0)
    }
    case e: Echo => {
      println(e.message)
    }
  }

  def main(args: Array[String]): Unit = {
    println("Keeping alive for 30 seconds...")
    Thread.sleep(30000)
  }
}

object Interpreter extends CommandInterpreter {
  override def toCommand(line: String): Option[Command] = line match {
    case "quit" => Some(Quit)
    case _ if line.startsWith("echo ") => Some(Echo(line.substring(5)))
    case _ => None
  }

  override def fromCommand(command: Command): String = command match {
    case Quit => "quit"
    case e: Echo => s"echo ${e.message}"
  }
}

object Quit extends Command

case class Echo(message: String) extends Command