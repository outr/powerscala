package org.powerscala.hierarchy

import annotation.tailrec

trait Parent extends Element {
  def contents: Seq[Element]

  override val hierarchy = new ParentHierarchy

  protected class ParentHierarchy extends ElementHierarchy {
    /**
     * The element before this one hierarchically.
     */
    def before(element: Element) = findBefore(null, element, contents)

    /**
     * The element after this one hierarchically.
     */
    def after(element: Element) = findAfter(null, element, contents)

    override def last = if (contents.nonEmpty) {
      contents.last.hierarchy.last
    } else {
      super.last
    }

    override def next = if (contents.nonEmpty) {
      contents.head
    } else {
      super.next
    }

    def hasDescendant[T](value: T, maxDepth: Int = Int.MaxValue)(implicit manifest: Manifest[T]) = descendant((t: T) => t == value, maxDepth)(manifest) != None

    /**
     * Returns true if the value passed is a child of this Parent.
     */
    def hasChild[T](value: T)(implicit manifest: Manifest[T]) = hasDescendant(value, 1)(manifest)

    /**
     * Uses the supplied matching function to return the first descendant match given the specified type or None if no
     * match is found.
     */
    def descendant[T](matcher: T => Boolean, maxDepth: Int = Int.MaxValue, children: Seq[Element] = contents)(implicit manifest: Manifest[T]): Option[T] = {
      if (children.nonEmpty) {
        // if (manifest.runtimeClass.isAssignableFrom(p.getClass) && matcher(p.asInstanceOf[T])) {
        val child = children.head
        if (manifest.runtimeClass.isAssignableFrom(child.asInstanceOf[AnyRef].getClass) && matcher(child.asInstanceOf[T])) {
          Option(child.asInstanceOf[T])
        } else {
          val result = descendant[T](matcher, maxDepth, children.tail)(manifest)
          if (result == None && maxDepth > 1) {
            child match {
              case parent: Parent => parent.hierarchy.descendant(matcher, maxDepth - 1, parent.contents)
              case _ => None
            }
          } else {
            result
          }
        }
      } else {
        None
      }
    }

    @tailrec
    private def findBefore(previous: Element, element: Element, children: Seq[Element]): Element = {
      if (children.nonEmpty) {
        val current = children.head
        if (current == element) {
          if (previous == null) {
            Parent.this
          } else {
            previous.hierarchy.last
          }
        } else {
          findBefore(current, element, children.tail)
        }
      } else {
        null
      }
    }

    @tailrec
    private def findAfter(previous: Element, element: Element, children: Seq[Element]): Element = {
      if (children.isEmpty) {
        if (parent != null) {
          parent.hierarchy.after(Parent.this)
        } else {
          null
        }
      } else {
        val current = children.head
        if (previous == element) {
          current
        } else {
          findAfter(current, element, children.tail)
        }
      }
    }
  }

}