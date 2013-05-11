package org.powerscala.hierarchy

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait MutableChildLike[P] extends ChildLike[P] {
  protected var hierarchicalParent: P = _
}

object MutableChildLike {
  def assignParent(child: MutableChildLike[_], parent: Any) = {
    child.asInstanceOf[MutableChildLike[Any]].hierarchicalParent = parent
  }
}