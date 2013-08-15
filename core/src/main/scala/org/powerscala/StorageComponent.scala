package org.powerscala

/**
 * StorageComponent leverages Storage to provide a single instance of the StorageComponent instance for the specified
 * instance (T). This allows an easy-to-use "has a" relationship for easy extensibility.
 *
 * Simply create an object that mixes in this trait and the associated class (C) with a private constructor.
 *
 * class Example private() {}
 *
 * object Example extends StorageComponent[Example, String]
 *
 * The above example allows tying an "Example" instance to a specific String.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait StorageComponent[C, T] {
  protected def componentIdentifier = getClass.getName

  /**
   * Retrieve an instance of C (create it if necessary). There can be only one for the supplied T.
   *
   * @param t the instance for the C representation
   * @return C
   */
  def apply(t: T) = Storage.getOrSet(t, componentIdentifier, create(t))

  protected def create(t: T): C
}
