package org.powerscala.reflect

import java.lang.reflect.{Modifier, Method}

/**
 * EnhancedMethod wraps a java.lang.reflect.Method to provide more functionality and easier access.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnhancedMethod protected[reflect](val parent: EnhancedClass, val declaring: EnhancedClass, val javaMethod: Method) {
  private lazy val _docs = declaring.getDocs.method(javaMethod)

  /**
   * The method name.
   */
  def name = javaMethod.getName

  /**
   * The arguments this method takes to invoke.
   */
  lazy val args: List[MethodArgument] = for ((dc, index) <- _docs.args.zipWithIndex) yield {
    new MethodArgument(index, dc.name, dc.`type`, getDefault(index), dc.doc)
  }

  /**
   * Returns true if the argument list of Tuple2 name -> type matches.
   */
  def hasArgs(argsList: List[(String, EnhancedClass)]) = {
    if (args.length == argsList.length) {
      argsList.forall(arg => argIndex(arg._1) != -1)
    } else {
      false
    }
  }

  /**
   * Returns the index of the argument defined by name.
   */
  def argIndex(arg: String) = args.find(ma => ma.name == arg) match {
    case None => -1
    case Some(ma) => args.indexOf(ma)
  }

  /**
   * Retrieves a MethodArgument by argument name.
   */
  def arg(name: String) = args.find(ma => ma.name == name)

  /**
   * The return type of this method.
   */
  def returnType = _docs.returnClass

  /**
   * Documentation for this method.
   */
  def docs = _docs.docs

  /**
   * The URL to access the documentation for this method.
   */
  def docsURL = _docs.url

  /**
   * True if this is a native method.
   */
  def isNative = Modifier.isNative(javaMethod.getModifiers)

  /**
   * True if this is a static method.
   */
  def isStatic = Modifier.isStatic(javaMethod.getModifiers)

  private def getDefault(index: Int) = {
    val instanceClass = parent.nonCompanion
    val defaultMethodName = name + "$default$" + (index + 1)
    instanceClass.method(defaultMethodName)
  }

  /**
   * Invokes this method on the supplied instance with the passed arguments.
   */
  def invoke[R](instance: AnyRef, args: Any*) = {
    if (args.length != this.args.length) {
      throw new IllegalArgumentException("%s arguments supplied, %s expected".format(args.length, this.args.length))
    }
    javaMethod.setAccessible(true)
    try {
      javaMethod.invoke(instance, args.map(a => a.asInstanceOf[AnyRef]): _*).asInstanceOf[R]
    } catch {
      case t: Throwable => throw new RuntimeException("Error(%s) attempting to invoke %s on %s with arguments: %s".format(t.getClass.getSimpleName, this, instance, args), t)
    }
  }

  /**
   * Invokes this method on the supplied instance with a list of argument names along with values.
   *
   * The arguments list does not have to have the same number of arguments as the method if the method provides default
   * values for the unsupplied argument names.
   */
  def apply[R](instance: AnyRef, args: Map[String, Any] = Map.empty): R = {
    val arguments = this.args.map {
      case methodArgument => args.get(methodArgument.name) match {
        case Some(value) => EnhancedMethod.convertTo(value, methodArgument.`type`)            // Value supplied directly
        case _ if (methodArgument.hasDefault) => methodArgument.default[Any](instance).get    // Method had a default argument
        case _ => methodArgument.`type`.defaultForType[Any]                                   // No argument found so we use the default for the type
      }
    }

    /*var map = Map.empty[String, Any]

    // Assign default values if specified
    this.args.foreach(arg => if (arg.hasDefault) map += arg.name -> arg.default[AnyRef](instance).get) // Assign defaults
    map ++= args

    val arguments = new Array[Any](map.size)

    // Validate all arguments have correct values
    this.args.foreach {
      case arg => {
        val value = map.getOrElse(arg.name, arg.`type`.defaultForType).asInstanceOf[AnyRef]   // Get the argument or assign the default for the type
        if (!arg.`type`.isCastable(value)) {
          throw new RuntimeException("Invalid class type %s (expected: %s) for %s.%s(%s) - %s".format(value.getClass, arg.`type`.javaClass, parent.simpleName, EnhancedMethod.this.name, arg.name, value))
        }
      }
    }

    // Put arguments into Array
    map.foreach(arg => {
      val index = argIndex(arg._1)
      if (index == -1) {
        throw new RuntimeException("Unable to find %s for method %s".format(arg._1, EnhancedMethod.this))
      }
      arguments(index) = arg._2
    })*/

    // Invoke method
    try {
      invoke[R](instance, arguments: _*)
    } catch {
      case t: Throwable => throw new IllegalArgumentException("Unable to invoke %s with args %s".format(this, args), t)
    }
  }

  private[reflect] def argsMatch(args: Seq[EnhancedClass]) = {
    args.length == javaMethod.getParameterTypes.length &&
      javaMethod.getParameterTypes.zip(args).forall(t => class2EnhancedClass(t._1) == t._2)
  }

  /**
   * The absolute absoluteSignature of this method including package and class name.
   */
  def absoluteSignature = declaring.name + "." + signature

  /**
   * The localized absoluteSignature of this method. Excludes class name and package.
   */
  def signature = {
    val b = new StringBuilder()
    b.append(name)
    b.append('(')
    b.append(args.mkString(", "))
    b.append("): ")
    b.append(javaMethod.returnType.`type`)
    b.toString()
  }

  override def toString = absoluteSignature
}

object EnhancedMethod {
  def convertTo(value: Any, resultType: EnhancedClass) = resultType.name match {
    case "Int" => value match {
      case b: Byte => b.toInt
      case c: Char => c.toInt
      case l: Long => l.toInt
      case f: Float => f.toInt
      case d: Double => d.toInt
    }
    case "Long" => value match {
      case b: Byte => b.toLong
      case c: Char => c.toLong
      case i: Int => i.toLong
      case f: Float => f.toLong
      case d: Double => d.toLong
    }
    case "Float" => value match {
      case b: Byte => b.toFloat
      case c: Char => c.toFloat
      case i: Int => i.toFloat
      case l: Long => l.toFloat
      case d: Double => d.toFloat
    }
    // TODO: add more type conversions
    case _ if (resultType.isCastable(value)) => value
    case _ => throw new RuntimeException("Unable to convert %s (%s) to %s".format(value, value.asInstanceOf[AnyRef].getClass.getName, resultType))
  }
}