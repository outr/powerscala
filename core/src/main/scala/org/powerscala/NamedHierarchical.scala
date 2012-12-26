package org.powerscala

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait NamedHierarchical extends Named with Hierarchical {
  def hierarchicalName: String = hierarchicalParent match {
    case Some(parent) => parent match {
      case n: NamedHierarchical => "%s.%s".format(n.hierarchicalName, name)
      case n: Named => "%s.%s".format(n.name, name)
      case _ => name
    }
    case None => name
  }
}