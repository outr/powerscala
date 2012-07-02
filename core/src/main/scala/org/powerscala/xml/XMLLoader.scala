package org.powerscala.xml

import xml.Elem

/**
 * XMLLoader converts an XML element to an object with the generic type defined by this instance.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait XMLLoader[T] {
  /**
   * Converts from the supplied Elem to T.
   */
  def apply(elem: Elem): T
}