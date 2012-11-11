package org.powerscala.reflect.uml

import org.powerscala.reflect.{CaseValue, EnhancedClass}

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ClassDiagramGenerator {
  private val baseURL = "http://yuml.me/diagram/plain/class/"

  private val cached = new ThreadLocal[Set[String]]

  def generateForClasses(classes: EnhancedClass*) = {
    cached.set(Set.empty[String])
    try {
      val data = classes.map(generateRecursively).flatten.mkString(",")
      "%s%s".format(baseURL, data)
    } finally {
      cached.set(null)
    }
  }

  private def generateRecursively(c: EnhancedClass): List[String] = {
    if (!cached.get().contains(c.name)) {
      cached.set(cached.get() + c.name)
      val entry = "[%s|%s{bg:orange}]".format(c.simpleName, c.caseValues.map(generateCaseValue).mkString(";"))
      val additional = c.caseValues.collect {
        case cv if (cv.valueType.isCase) => {   // TODO: add modularity to support special handling for classes (ie. Lazy and LazyList)
          "[%s]->[%s]".format(c.simpleName, cv.valueType.simpleName) :: generateRecursively(cv.valueType)
        }
      }.flatten
      entry :: additional
    } else {
      Nil
    }
  }

  private def generateCaseValue(cv: CaseValue) = {
    "%s: %s".format(cv.name, cv.valueType.simpleName)
  }

  def main(args: Array[String]) = {
    val url = ClassDiagramGenerator.generateForClasses(classOf[Test], classOf[Test2], classOf[Test3])
    println(url)
  }
}

case class Test(one: String, two: Int, three: Test2)

case class Test2(name: String, test: Test)

case class Test3(testing: String)