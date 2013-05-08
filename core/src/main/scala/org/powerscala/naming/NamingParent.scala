package org.powerscala.naming

import org.powerscala.reflect._
import collection.mutable.ArrayBuffer
import org.powerscala.hierarchy.Named

/**
 * NamingParent can be mixed into a class to access fields from their names.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait NamingParent {
  implicit val namingParentInstance = this
  protected[naming] val namedFields = new ArrayBuffer[Named]()

  protected[powerscala] def add(child: Named) = synchronized {
    if (!namedFields.contains(child)) {
      namedFields += child
    }
  }

  protected[naming] def name(child: NamedChild) = getClass.methods.find(m => if (m.javaMethod.getParameterTypes.isEmpty && m.returnType.`type`.name != "Unit") {
    m[Any](this) == child
  } else {
    false
  }).map(m => m.name).getOrElse(null)

  protected[naming] def notFound(name: String) = {
    throw new NullPointerException("Unable to find %s.%s".format(getClass.name, name))
  }
}