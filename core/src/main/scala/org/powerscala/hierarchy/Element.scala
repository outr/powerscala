package org.powerscala.hierarchy


/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Element extends Child {
  private var _parent: Parent = _
  def parent = _parent

  val hierarchy = new ElementHierarchy

  /**
   * Invokes the supplied method if this class matches the supplied generic type.
   *
   * Returns true if the generic type matched.
   */
  def apply[T](f: T => Unit)(implicit manifest: Manifest[T]): Boolean = {
    if (manifest.erasure.isAssignableFrom(getClass)) {
      f(this.asInstanceOf[T])
      true
    }
    else {
      false
    }
  }

  protected class ElementHierarchy {
    /**
     * The element before this one or null.
     */
    def previous = parent match {
      case null => null
      case p => p.hierarchy.before(Element.this)
    }

    /**
     * The element after this one or null.
     */
    def next = parent match {
      case null => null
      case p => p.hierarchy.after(Element.this)
    }

    def iterator[E <: Element](implicit manifest: Manifest[E]) = {
      new Iterator[E] {
        private var _next = forward[E]()(manifest)

        def hasNext = _next != null

        def next() = _next match {
          case null => null.asInstanceOf[E]
          case n => {
            _next = n.hierarchy.forward[E]()(manifest)
            n
          }
        }
      }
    }

    /**
     * Moves backward until it finds an Element matching the condition or null if it reaches the beginning.
     */
    def backward[E <: Element](condition: E => Boolean = (e: E) => true)(implicit manifest: Manifest[E]): E = {
      previous match {
        case null => null.asInstanceOf[E]
        case element if (matchesManifest(element, manifest) && condition(element.asInstanceOf[E])) => {
          element.asInstanceOf[E]
        }
        case element => element.hierarchy.backward[E](condition)(manifest)
      }
    }

    /**
     * Moves forward until it finds an Element matching the condition or null if it reaches the end.
     */
    def forward[E <: Element](condition: E => Boolean = (e: E) => true)(implicit manifest: Manifest[E]): E = {
      next match {
        case null => null.asInstanceOf[E]
        case element if (matchesManifest(element, manifest) && condition(element.asInstanceOf[E])) => {
          element.asInstanceOf[E]
        }
        case element => element.hierarchy.forward[E](condition)(manifest)
      }
    }

    /**
     * The first element hierarchically.
     */
    def first: Element = parent match {
      case null => Element.this
      case element => element.hierarchy.first
    }

    protected def matchesManifest[T <: AnyRef](value: AnyRef, manifest: Manifest[T]) = manifest.erasure.isAssignableFrom(value.getClass)

    /**
     * The last element hierarchically from this level.
     */
    def last = Element.this

    /**
     * Returns true if the value passed is in the ancestry hierarchy for this Child.
     */
    def hasAncestor[T](value: T, maxDepth: Int = Int.MaxValue)(implicit manifest: Manifest[T]) = {
      Child.hasAncestor[T](Element.this, value, maxDepth)
    }

    /**
     * Returns true if the value passed is the parent of this Child.
     */
    def hasParent[T](value: T)(implicit manifest: Manifest[T]) = hasAncestor(value, 1)(manifest)

    /**
     * Uses the supplied matching function to return the first ancestor match given the specified type or None if no
     * match is found.
     */
    def ancestor[T](matcher: T => Boolean, maxDepth: Int = Int.MaxValue)(implicit manifest: Manifest[T]): Option[T] = {
      Child.ancestor[T](Element.this, matcher, maxDepth)(manifest)
    }
  }
}

object Element {
  def assignParent(element: Element, parent: Parent) = element._parent = parent
}