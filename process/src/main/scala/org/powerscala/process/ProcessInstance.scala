package org.powerscala.process

import org.powerscala.concurrent.Executor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait ProcessInstance {
  protected var _state = ProcessState.NotStarted
  protected var _throwable: Throwable = _
  def state = _state

  def name: String

  def start(synchronous: Boolean) = {
    _state = ProcessState.Starting
    if (synchronous) {
      executeProcess()
    } else {
      Executor.invoke {
        executeProcess()
      }
    }
  }

  private def executeProcess() = try {
    _state = ProcessState.Running
    run()
    _state = ProcessState.Finished
  } catch {
    case t: Throwable => {
      _state = ProcessState.Error
      throw t
    }
  }

  def stop(): Boolean

  def run(): Unit

  def error = Option(_throwable)
}
