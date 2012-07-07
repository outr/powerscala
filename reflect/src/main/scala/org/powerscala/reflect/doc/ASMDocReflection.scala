package org.powerscala.reflect.doc

import java.lang.reflect.Method
import org.objectweb.asm.{ClassReader, Type}
import org.objectweb.asm.tree.{LocalVariableNode, MethodNode, ClassNode}

import scala.collection.JavaConversions._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class ASMDocReflection(clazz: Class[_]) extends DocumentationReflection {
  lazy val classNode = ASMDocReflection.classNode(clazz)
  lazy val methods = classNode.methods.toList.asInstanceOf[List[MethodNode]]
  private var documentation = Map.empty[Method, MethodDocumentation]

  def method(m: Method) = documentation.get(m) match {
    case Some(doc) => doc
    case None => generateDocumentation(m)
  }

  private def generateDocumentation(m: Method) = synchronized {
    try {
      val name = m.getName
      val returnClass = DocumentedClass(null, m.getReturnType, None)
      val md = if (m.getParameterTypes.length > 0) {
        val desc = Type.getMethodDescriptor(m)
        val results = methods.collect {
          case methodNode if (methodNode.name == name && methodNode.desc == desc) => methodNode
        }
        if (results.length != 1) {
          throw new RuntimeException("%s methodNodes with the supplied signature: %s".format(results.length, m))
        }
        val mn = results.head
        val variables = mn.localVariables
        val args = if (variables.length == 0) {
          m.getParameterTypes.zipWithIndex.map {
            case (c, index) => DocumentedClass("arg%s".format(index), c, None)
          }.toList
        } else {
          val argNames = variables.tail.map(lvn => lvn.asInstanceOf[LocalVariableNode].name).toList
          argNames.zip(m.getParameterTypes).map {
            case (n, c) => DocumentedClass(n, c, None)
          }
        }
        MethodDocumentation(args, returnClass, null, None)
      } else {
        MethodDocumentation(Nil, returnClass, null, None)
      }
      documentation += m -> md
      md
    } catch {
      case t => throw new RuntimeException("Unable to generate documentation for %s".format(m), t)
    }
  }
}

object ASMDocReflection extends DocMapper {
  def apply(c: Class[_]) = new ASMDocReflection(c)

  def classNode(clazz: Class[_]) = {
    val classLoader = clazz.getClassLoader
    val declaringType = Type.getType(clazz)
    val url = declaringType.getInternalName + ".class"
    val classNode = new ClassNode()
    val input = classLoader.getResourceAsStream(url)
    try {
      val classReader = new ClassReader(input)
      classReader.accept(classNode, 0)
    } finally {
      input.close()
    }
    classNode
  }
}