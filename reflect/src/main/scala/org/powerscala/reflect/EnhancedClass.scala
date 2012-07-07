package org.powerscala.reflect

import doc.DocumentationReflection
import ref.SoftReference
import java.lang.reflect.{Modifier, Method}

/**
 * Wraps a Class to provide more powerful functionality.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class EnhancedClass protected[reflect](val javaClass: Class[_]) {
  /**
   * The name of the class.
   */
  lazy val name = EnhancedClass.convertClass(javaClass)

  lazy val simpleName = name.contains(".") match {
    case true => name.substring(name.lastIndexOf('.') + 1)
    case false => name
  }

  /**
   * All constructors on this class.
   */
  lazy val constructors: List[EnhancedConstructor] = javaClass.getConstructors.toList.map(c => new EnhancedConstructor(this, c))

  /**
   * All methods on this class.
   */
  lazy val methods: List[EnhancedMethod] = {
    val javaMethods = javaClass.getMethods.toSet ++ javaClass.getDeclaredMethods.toSet
    javaMethods.toList.map(m => new EnhancedMethod(this, EnhancedClass(m.getDeclaringClass), m))
  }

  /**
   * Finds a method by the absoluteSignature.
   */
  def methodBySignature(signature: String) = methods.find(m => m.absoluteSignature == signature || m.signature == signature)

  /**
   * Finds a method from the supplied name and args.
   */
  def method(name: String, args: EnhancedClass*): Option[EnhancedMethod] = methods.find(m => m.name == name && m.argsMatch(args))

  /**
   * Finds a method from the supplied name and argument names and values.
   */
  def methodByArgs(name: String, args: List[(String, Any)]) = method(name, args.map(t => t._1 -> EnhancedClass.fromValue(t._2)))

  /**
   * Finds a method from the supplied name and argument names and types.
   */
  def method(name: String, args: List[(String, EnhancedClass)]) = methods.find(m => m.hasArgs(args))

  /**
   * Finds the first method match for the name supplied.
   */
  def methodByName(name: String) = methods.find(m => m.name == name)

  /**
   * Singleton instance on the associated companion object. This can be called on the object or class to get the
   * instance for the companion object.
   */
  lazy val instance = {
    companion match {
      case Some(clazz) => clazz.javaClass.getFields.find(f => f.getName == "MODULE$").map(f => f.get(null))
      case None => None
    }
  }

  /**
   * True if this is a companion object.
   */
  def isCompanion = javaClass.getName.endsWith("$")

  /**
   * True if this is a case class
   */
  def isCase = copyMethod != None

  /**
   * True if this is a transient class
   */
  def isTransient = Modifier.isTransient(javaClass.getModifiers)

  /**
   * The companion class to this class if it exists. If this is the companion class it will return itself.
   */
  lazy val companion: Option[EnhancedClass] = try {
    if (isCompanion) {
      Some(this)
    } else {
      Some(Class.forName(javaClass.getName + "$"))
    }
  } catch {
    case exc: ClassNotFoundException => None
  }
  // TODO: add support for class-level docs

  /**
   * CaseValue instances representing the arguments if this is a case class.
   */
  lazy val caseValues = copyMethod.map(method => method.args.map(arg => CaseValue(arg.name, arg.`type`, EnhancedClass.this))).getOrElse(Nil)

  /**
   * The method used to create a copy of an instance if this is a case class.
   */
  lazy val copyMethod = methodByName("copy")

  /**
   * Retrieve a CaseValue by name if this is a case class.
   */
  def caseValue(name: String) = caseValues.find(cv => cv.name == name)

  /**
   * Reflective copy of a case class with the supplied arguments.
   *
   * Note that an empty arguments list may be supplied to create a clone.
   */
  def copy[T](instance: T, args: Map[String, Any]) = {
    if (copyMethod == None && args.isEmpty) {
      companion match {
        case Some(clazz) => clazz.method("apply") match {
          case Some(method) => method[T](clazz.instance.get)
          case None => throw new NullPointerException("No copy method for this class (%s) and unable to find empty apply method on companion.".format(name))
        }
        case None => throw new NullPointerException("No copy method for this class (%s) and unable to find companion.".format(name))
      }
    } else {
      val cm = copyMethod.getOrElse(throw new NullPointerException("No copy method for this class (%s)".format(name)))
      cm[T](instance.asInstanceOf[AnyRef], args)
    }
  }

  /**
   * Method used to create case class instances or None if this is not a case class.
   */
  lazy val createMethod = companion.map(c => c.method("apply", caseValues.map(cv => cv.name -> cv.valueType)).getOrElse(null))

  /**
   * Reflective invocation of the generated apply method on a companion to this case class.
   *
   * Note that any default arguments may optionally be omitted.
   */
  def create[T](args: Map[String, Any]): T = {
    val companionClass = companion.getOrElse(throw new NullPointerException("No companion class for %s".format(this)))
    val applyMethod = createMethod.getOrElse(throw new NullPointerException("No apply method for args %s".format(caseValues)))
    applyMethod[T](companionClass.instance.getOrElse(throw new NullPointerException("No companion instance found for %s".format(this))), args)
  }

  // Only called internally to avoid receiving methods not for this class
  private[reflect] def apply(m: Method): EnhancedMethod = {
    method(m.getName, m.getParameterTypes.map(c => class2EnhancedClass(c)): _*).get
  }

  protected[reflect] def getDocs = _docs.get match {
    case Some(df) => df
    case None => {
      _docs = generateDocRef()
      _docs()
    }
  }

  private var _docs = generateDocRef()

  private def generateDocRef() = new SoftReference[DocumentationReflection](DocumentationReflection(javaClass))

  override def toString = name

  override def equals(ref: Any) = ref match {
    case ec: EnhancedClass => javaClass == ec.javaClass
    case c: Class[_] => javaClass == c
    case _ => false
  }

  /**
   * Returns the default value by type. For primitives this will return zero or false and for references this will
   * return null.
   */
  def defaultForType: Any = EnhancedClass.convertClass(javaClass) match {
    case "Boolean" => false
    case "Byte" => 0.byteValue()
    case "Int" => 0
    case "Long" => 0.longValue()
    case "Float" => 0.floatValue()
    case "Double" => 0.doubleValue()
    case "List" => Nil
    case _ => null
  }
}

object EnhancedClass {
  // Set up defaults
  val Integer = EnhancedClass(classOf[Int])

  register(classOf[java.lang.Integer], Integer)

  def apply(clazz: Class[_]) = class2EnhancedClass(clazz)

  def fromValue(value: Any) = value match {
    case null => null
    case ref: AnyRef => apply(ref.getClass)
  }

  /**
   * Converts primitive classes to wrapper classes.
   */
  def convertClass(c: Class[_]) = c.getName match {
    case "boolean" => "Boolean"
    case "java.lang.Boolean" => "Boolean"
    case "byte" => "Byte"
    case "java.lang.Byte" => "Byte"
    case "int" => "Int"
    case "java.lang.Integer" => "Int"
    case "long" => "Long"
    case "java.lang.Long" => "Long"
    case "float" => "Float"
    case "java.lang.Float" => "Float"
    case "double" => "Double"
    case "java.lang.Double" => "Double"
    case "void" => "Unit"
    case "java.lang.String" => "String"
    case "[I" => "Array[Int]"
    case "scala.collection.immutable.Nil$" => "List"
    case s => s
  }
}