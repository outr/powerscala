package org.powerscala.reflect

import doc.DocumentationReflection
import ref.SoftReference
import java.lang.reflect.{Modifier, Method}
import org.reflections.Reflections

import scala.collection.JavaConversions._
import annotation.tailrec

import language.existentials

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

  lazy val fields: List[EnhancedField] = {
    val javaFields = javaClass.getFields.toSet ++ javaClass.getDeclaredFields.toSet
    javaFields.toList.map(f => new EnhancedField(this, EnhancedClass(f.getDeclaringClass), f))
  }

  /**
   * All methods on this class.
   */
  lazy val methods: List[EnhancedMethod] = {
    val javaMethods = javaClass.getMethods.toSet ++ javaClass.getDeclaredMethods.toSet
    javaMethods.toList.map(m => new EnhancedMethod(this, EnhancedClass(m.getDeclaringClass), m))
  }

  /**
   * All classes that are subtypes of this class in the runtime. Not an inexpensive operation.
   */
  lazy val subTypes = new Reflections("").getSubTypesOf(javaClass).toList.map(c => EnhancedClass(c))

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
  def method(name: String, args: List[(String, EnhancedClass)]) = methods.find(m => m.name == name && m.hasArgs(args))

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
   * Determines all super class and interfaces for this class and returns them in an ordered list working backwards from
   * this class up the tree.
   */
  def parents = {
    var list = List.empty[Class[_]]
    def addParentsOf(clazz: Class[_]): Unit = {
      val parent = clazz.getSuperclass
      var temporal = List.empty[Class[_]]
      if (parent != null && !list.contains(parent)) {
        temporal = parent :: temporal
      }
      clazz.getInterfaces.foreach {
        case i => if (!list.contains(i)) {
          temporal = i :: temporal
        }
      }
      list = temporal ::: list
      temporal.foreach {
        case c => addParentsOf(c)
      }
    }

    addParentsOf(javaClass)
    list.reverse
  }

  /**
   * True if this is a companion object.
   */
  lazy val isCompanion = javaClass.getName.endsWith("$") && javaClass.getFields.find(f => f.getName == "MODULE$").nonEmpty

  /**
   * True if this is a case class
   */
  def isCase = !javaClass.isArray && copyMethod != None

  /**
   * True if this is a transient class
   */
  def isTransient = Modifier.isTransient(javaClass.getModifiers)

  /**
   * Iterates up the class hierarchy to find a companion of the specified type.
   *
   * @param manifest of type T
   * @tparam T the class of the companion
   * @return Option[T]
   */
  def findCompanionOfType[T](implicit manifest: Manifest[T]) = if (hasCompanion[T](manifest)) {
    Some(instance.get.asInstanceOf[T])
  } else {
    val classes = parents
    classes.find(c => c.hasCompanion[T](manifest)).map(c => c.instance.get.asInstanceOf[T])
  }

  def hasCompanion[T](implicit manifest: Manifest[T]) = companion match {
    case Some(c) => c.hasType(manifest.runtimeClass)
    case None => false
  }

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

  lazy val nonCompanion: EnhancedClass = if (isCompanion) {
    Class.forName(javaClass.getName.substring(0, javaClass.getName.length - 1))
  } else {
    this
  }

  /**
   * CaseValue instances representing the arguments if this is a case class.
   */
  lazy val caseValues = copyMethod.map(method => method.args.map(arg => CaseValue(arg.name, arg.`type`, EnhancedClass.this))).getOrElse(Nil)

  /**
   * The method used to create a copy of an instance if this is a case class.
   */
  lazy val copyMethod = methodByName("copy")

  /**
   * Retrieves the constructor that matches the copy method.
   */
  lazy val copyConstructor = copyMethod match {
    case Some(m) => {
      val cmArgs = m.javaMethod.getParameterTypes.map(c => c.getSimpleName).mkString(", ")
      constructors.find(c => c.javaConstructor.getParameterTypes.map(c => c.getSimpleName).mkString(", ") == cmArgs)
    }
    case None => None
  }

  /**
   * Retrieve a CaseValue by name if this is a case class.
   */
  def caseValue(name: String) = caseValues.find(cv => cv.name == name)

  /**
   * Utilizes case classes to derive the value of the field defined by 'name' on the given 'instance'.
   *
   * @param instance the instance for this class
   * @param name the dot-separated hierarchical field structure.
   * @tparam T the return type
   * @return T or NPE
   */
  @tailrec
  final def value[T](instance: AnyRef, name: String): T = {
    val hasMore = name.indexOf('.') != -1
    val n = if (hasMore) {
      name.substring(0, name.indexOf('.'))
    } else {
      name
    }
    val cv = caseValue(n).getOrElse(throw new NullPointerException("Unable to find %s.%s for %s".format(this.name, n, name)))
    val result = cv.apply[Any](instance)
    if (!hasMore) {
      result.asInstanceOf[T]
    } else {
      cv.valueType.value[T](result.asInstanceOf[AnyRef], name.substring(name.indexOf('.') + 1))
    }
  }

  /**
   * Utilizes case classes to copy the hierarchical value and return a new instance of the case class with the new
   * value.
   *
   * @param instance the instance to copy
   * @param name the dot-separated hierarchical field structure.
   * @param value the new value to set
   * @tparam T the copied instance with the new value
   * @return copied instance T
   */
  final def modify[T](instance: T, name: String, value: Any): T = {
    val hasMore = name.indexOf('.') != -1
    val n = if (hasMore) {
      name.substring(0, name.indexOf('.'))
    } else {
      name
    }
    val cv = caseValue(n).getOrElse(throw new NullPointerException("Unable to find %s.%s for %s".format(this.name, n, name)))
    if (!hasMore) {
      cv.copy[T](instance, value)
    } else {
      val result = cv.apply[AnyRef](instance.asInstanceOf[AnyRef])
      val modified = cv.valueType.modify[Any](result, name.substring(name.indexOf('.') + 1), value)
      cv.copy[T](instance, modified)
    }
  }

  /**
   * Reflective copy of a case class with the supplied arguments.
   *
   * Note that an empty arguments list may be supplied to create a clone.
   */
  def copy[T](instance: T, args: Map[String, Any] = Map.empty, requireValues: Boolean = false) = {
    if (copyMethod == None && args.isEmpty) {   // Instantiation via empty apply method on companion in no copy
      companion match {
        case Some(clazz) => clazz.method("apply") match {
          case Some(method) => method[T](clazz.instance.get)
          case None => throw new NullPointerException("No copy method for this class (%s) and unable to find empty apply method on companion.".format(name))
        }
        case None => throw new NullPointerException("No copy method for this class (%s) and unable to find companion.".format(name))
      }
    } else {
      if (instance == null) {
        val cc = copyConstructor.getOrElse(throw new NullPointerException("No copy constructor for this class (%s)".format(name)))
        cc[T](args, requireValues = requireValues)
      } else {
        val cm = copyMethod.getOrElse(throw new NullPointerException("No copy method for this class (%s)".format(name)))
        cm[T](instance.asInstanceOf[AnyRef], args, requireValues = requireValues)
      }
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
   * Returns true if <code>v</code> is castable to this class type.
   */
  def isCastable(v: Any) = v match {
    case null => !javaClass.isPrimitive
    case b: Boolean => javaClass == classOf[Boolean]
    case b: Byte => javaClass == classOf[Byte]
    case i: Int => javaClass == classOf[Int]
    case l: Long => javaClass == classOf[Long]
    case f: Float => javaClass == classOf[Float]
    case d: Double => javaClass == classOf[Double]
    case _ => javaClass.isAssignableFrom(v.asInstanceOf[AnyRef].getClass)
  }

  /**
   * Returns true if <code>c</code> is extended or mixed in to this EnhancedClass.
   */
  def hasType(c: Class[_]) = c.isAssignableFrom(javaClass)

  /**
   * Returns the default value by type. For primitives this will return zero or false and for references this will
   * return null.
   */
  def defaultForType[T]: T = (EnhancedClass.convertClass(javaClass) match {
    case "Boolean" => false
    case "Byte" => 0.byteValue()
    case "Int" => 0
    case "Long" => 0.longValue()
    case "Float" => 0.floatValue()
    case "Double" => 0.doubleValue()
    case "List" => Nil
    case _ => null
  }).asInstanceOf[T]

  /**
   * Attempts to convert the supplied value to the type of this class.
   *
   * @param value the value to convert
   * @tparam T the generic type of the return
   * @return T
   */
  def convertTo[T](name: String, value: Any) = EnhancedMethod.convertTo(name, value, this).asInstanceOf[T]
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
    case "short" => "Short"
    case "java.lang.Short" => "Short"
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

  def convertPrimitives(c: Class[_]) = if (c == classOf[Boolean]) {
    classOf[java.lang.Boolean]
  } else if (c == classOf[Byte]) {
    classOf[java.lang.Byte]
  } else if (c == classOf[Short]) {
    classOf[java.lang.Short]
  } else if (c == classOf[Char]) {
    classOf[java.lang.Character]
  } else if (c == classOf[Int]) {
    classOf[java.lang.Integer]
  } else if (c == classOf[Long]) {
    classOf[java.lang.Long]
  } else if (c == classOf[Float]) {
    classOf[java.lang.Float]
  } else if (c == classOf[Double]) {
    classOf[java.lang.Double]
  } else {
    c
  }
}