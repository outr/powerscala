package org.powerscala.reflect

import java.lang.reflect.Constructor

/**
 * EnhancedConstructor wraps a java.lang.reflect.Constructor to provide more functionality and easier access.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnhancedConstructor protected[reflect](val parent: EnhancedClass, val javaConstructor: Constructor[_]) {
  private lazy val _docs = parent.getDocs.constructor(javaConstructor)

  /**
   * The constructor's name.
   */
  def name = javaConstructor.getName

  /**
   * The arguments this method takes to invoke.
   */
  lazy val args: List[MethodArgument] = for ((dc, index) <- _docs.args.zipWithIndex) yield {
    new MethodArgument(index, dc.name, dc.`type`, getDefault(index), dc.doc)
  }

  private def getDefault(index: Int) = {
    val defaultMethodName = "apply$default$" + (index + 1)
    parent.method(defaultMethodName)
  }

  def apply[T](args: Map[String, Any] = Map.empty, requireValues: Boolean = false) = {
    val params = this.args.map {
      case arg if (args.contains(arg.name)) => args(arg.name)                                         // Provided value
      case arg if (arg.hasDefault) => arg.default[Any](null).getOrElse(arg.`type`.defaultForType)     // Default value
      case arg if (!requireValues) => arg.`type`.defaultForType                                       // Default for type
      case arg => throw new RuntimeException("No value supplied for %s.".format(arg.name))
    }.asInstanceOf[List[AnyRef]]
    try {
      javaConstructor.newInstance(params: _*).asInstanceOf[T]
    } catch {
      case t: Throwable => throw new RuntimeException("Failed to instantiate %s with %s".format(this, params.mkString(", ")), t)
    }
  }

  /**
   * The absolute absoluteSignature of this method including package and class name.
   */
  def absoluteSignature = parent.name + "." + signature

  /**
   * The localized absoluteSignature of this method. Excludes class name and package.
   */
  def signature = {
    val b = new StringBuilder()
    b.append(name)
    b.append('(')
    b.append(args.mkString(", "))
    b.append("): ")
    b.append(parent)
    b.toString()
  }

  override def toString = absoluteSignature
}