package org.powerscala.command

import java.io.{BufferedReader, InputStreamReader}

class StandardIO extends CommandImplementation {
  private var keepAlive = true
  private var manager: CommandManager = _

  private lazy val reader = new BufferedReader(new InputStreamReader(System.in))
  private lazy val thread = new Thread(new Runnable {
    override def run(): Unit = while(keepAlive) {
      val line = reader.readLine()
      manager.interpreter.toCommand(line) match {
        case Some(command) => manager.process(command)
        case None => // Not a known command
      }
    }
  })
  override def start(manager: CommandManager): Unit = {
    this.manager = manager
    thread.setDaemon(true)
    thread.start()
  }

  override def send(line: String): Unit = println(line)

  override def dispose(): Unit = {
    keepAlive = false
    thread.interrupt()
  }
}
