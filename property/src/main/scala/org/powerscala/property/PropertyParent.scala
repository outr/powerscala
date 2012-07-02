package org.powerscala.property

import org.powerscala.naming.{NamingFilter, NamingParent}
import org.powerscala.hierarchy.{Named, Child}


/**
 * PropertyParent leverages NamingParent to define a "properties" value listing all properties
 * contained within this class.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait PropertyParent extends NamingParent with Child with Named {
  def parent: PropertyParent

  parent match {
    case p: NamingParent => p.add(this)
    case _ => // Missed
  }

  implicit val childrenParent = this

  /**
   * Access all the properties associated with this class.
   */
  val properties = new NamingFilter[Property[_]](this) {
    def apply[T](name: String) = super.apply(name).asInstanceOf[Property[T]]
  }

  /**
   * Access all the properties associated with this class and hierarchically access all properties associated with child
   * PropertyParents.
   */
  val allProperties = new NamingFilter[Property[_]](this, PropertyParent.hierarchicalProperties) {
    def apply[T](name: String) = super.apply(name).asInstanceOf[Property[T]]
  }
}

class ExplicitPropertyParent(val name: String, val parent: PropertyParent = null) extends PropertyParent

object PropertyParent {
  val hierarchicalProperties: PartialFunction[Named, List[Property[_]]] = {
    case property: Property[_] => List(property)
    case propertyParent: PropertyParent => propertyParent.allProperties.toList
    case _ => Nil
  }
}