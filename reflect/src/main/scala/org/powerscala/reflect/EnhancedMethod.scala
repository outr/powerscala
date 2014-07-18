package org.powerscala.reflect

import java.lang.reflect.{Modifier, Method}
import java.io.File
import org.powerscala.LocalStack
import argonaut.Json
import java.sql.Timestamp

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
  lazy val args: List[MethodArgument] = if (javaMethod.getParameterTypes.length == 0) {
    Nil
  } else {
    _docs.args.zipWithIndex.map {
      case (documentedClass, index) => new MethodArgument(index, documentedClass.name, documentedClass.`type`, getDefault(index), documentedClass.doc)
    }
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

  def isPublic = Modifier.isPublic(javaMethod.getModifiers)

  def isAbstract = Modifier.isAbstract(javaMethod.getModifiers)
  def isFinal = Modifier.isFinal(javaMethod.getModifiers)
  def isInterface = Modifier.isInterface(javaMethod.getModifiers)
  def isPrivate = Modifier.isPrivate(javaMethod.getModifiers)
  def isProtected = Modifier.isProtected(javaMethod.getModifiers)
  def isStrict = Modifier.isStrict(javaMethod.getModifiers)
  def isSynchronized = Modifier.isSynchronized(javaMethod.getModifiers)
  def isTransient = Modifier.isTransient(javaMethod.getModifiers)
  def isVolatile = Modifier.isVolatile(javaMethod.getModifiers)

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
  def apply[R](instance: AnyRef, args: Map[String, Any] = Map.empty, requireValues: Boolean = false): R = {
    val arguments = this.args.map {
      case methodArgument => args.get(methodArgument.name) match {
        case Some(value) => EnhancedMethod.convertTo(methodArgument.name, value, methodArgument.`type`)            // Value supplied directly
        case _ if methodArgument.hasDefault => methodArgument.default[Any](instance).get    // Method had a default argument
        case _ if !requireValues => methodArgument.`type`.defaultForType[Any]               // No argument found so we use the default for the type
        case _ => throw MissingArgumentException(methodArgument.name, s"No value supplied for '${methodArgument.name}'.")
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
  val converter = new LocalStack[(String, Any, EnhancedClass) => Any]

  def convertTo(name: String, value: Any, resultType: EnhancedClass) = convertToOption(name, value, resultType) match {
    case Some(result) => result
    case None => throw new RuntimeException("EnhancedMethod.convertTo: Unable to convert %s (%s) to %s".format(value, value.asInstanceOf[AnyRef].getClass.getName, resultType.name))
  }
  def convertToOption(name: String, value: Any, resultType: EnhancedClass): Option[Any] = {
    value match {
      case json: Json => {                      // Convert Json into useful types
        val converted = if (json.isArray) {
          json.arrayOrEmpty
        } else if (json.isBool) {
          json.bool.getOrElse(false)
        } else if (json.isNull) {
          null
        } else if (json.isNumber) {
          json.numberOrZero
        } else if (json.isString) {
          json.stringOrEmpty
        }
        convertToOption(name, converted, resultType)
      }
      case _ => {
        resultType.name match {
          case _ if resultType.isCastable(value) => Some(value)   // No conversion necessary
          case "[D" => Some(value match {
            case seq: Seq[_] => seq.asInstanceOf[Seq[Double]].toArray[Double]
          })
          case "Int" => Some(value match {
            case null => 0
            case b: Byte => b.toInt
            case c: Char => c.toInt
            case l: Long => l.toInt
            case f: Float => f.toInt
            case d: Double => d.toInt
            case i: java.lang.Integer => i.intValue()
            case s: String => s.toInt
            case s: Some[_] => s.get
            case None => 0
          })
          case "Long" => Some(value match {
            case null => 0L
            case b: Byte => b.toLong
            case c: Char => c.toLong
            case i: Int => i.toLong
            case f: Float => f.toLong
            case d: Double => d.toLong
            case l: java.lang.Long => l.longValue()
            case ts: Timestamp => ts.getTime
            case s: String => s.toLong
          })
          case "Float" => Some(value match {
            case null => 0.0f
            case b: Byte => b.toFloat
            case c: Char => c.toFloat
            case i: Int => i.toFloat
            case l: Long => l.toFloat
            case d: Double => d.toFloat
            case f: java.lang.Float => f.floatValue()
            case s: String => s.toFloat
          })
          case "Double" => Some(value match {
            case null => 0.0
            case b: Byte => b.toDouble
            case c: Char => c.toDouble
            case i: Int => i.toDouble
            case l: Long => l.toDouble
            case f: java.lang.Double => f.doubleValue()
            case s: String => s.toDouble
          })
          case "Boolean" => value match {
            case null => Some(false)
            case s: String => Some(s.toBoolean)
          }
          case "scala.math.BigDecimal" => value match {
            case d: java.math.BigDecimal => Some(BigDecimal(d))
          }
          case "java.io.File" => value match {
            case s: String => Some(new File(s))
          }
          case "java.lang.Object" => value match {
            case d: Double => Some(d.asInstanceOf[AnyRef])
          }
          case "String" => value match {
            case b: Boolean => Some(b.toString)
          }
          case "scala.Option" => Some(Option(value))
          case "scala.collection.immutable.List" => value match {
            case m: Map[_, _] => {
              Some(m.toList.collect {
                case (k, v) if intOption(k).nonEmpty => intOption(k).get -> v
              }.sortBy(t => t._1).map(t => t._2))
            }
          }
          case _ if value.isInstanceOf[Option[_]] => Some(value.asInstanceOf[Option[_]].getOrElse(null))
          case _ if converter.nonEmpty => Some(converter()(name, value, resultType))
          case _ => None
        }
      }
    }
  }

  private def intOption(v: Any) = v match {
    case s: String => try {
      Some(s.toInt)
    } catch {
      case t: Throwable => None
    }
    case i: Int => Some(i)
    case _ => None
  }
}

case class MissingArgumentException(argument: String, message: String) extends RuntimeException(message)