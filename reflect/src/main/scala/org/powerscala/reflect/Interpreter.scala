package org.powerscala.reflect

import tools.nsc.Settings
import tools.nsc.interpreter.IMain
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class Interpreter {
  private val settings = new Settings {
    usejavacp.value = true
  }
  private val interpreter = new IMain(settings)
  private val reference = new InterpreterReference
  bind("interpreterReference", reference)

  def bind[T : Manifest](name: String, value: T) = interpreter.beQuietDuring {
    interpreter.bind[T](name, value)
  }

  def importPackage(packageName: String) = eval("import %s".format(packageName), beQuiet = true)

  def eval(code: String, beQuiet: Boolean = false) = if (beQuiet) {
    interpreter.beQuietDuring {
      interpreter.interpret(code)
    }
  } else {
    interpreter.interpret(code)
  }

  def evalAndReturn[T](code: String): T = {
    eval("interpreterReference.set(%s)".format(code))
    reference[T]()
  }

  def reset() = interpreter.reset()
}

class InterpreterReference {
  private val ref = new AtomicReference[Any]()

  def set[T](value: T) = ref.set(value)

  def apply[T]() = ref.get().asInstanceOf[T]
}