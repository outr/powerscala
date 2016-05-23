package org.powerscala.collection

/**
  * Bi-Directional `Map` that adds `getLeft`, `getRight`, `left`, and `right` in order to access values in a specific
  * way. Utilizes two underlying maps to give improved lookup speed for value for key or key for value lookups.
  *
  * @tparam A the left type
  * @tparam B the right type
  */
class BiMap[A, B] extends Map[A, B] {
  private var forward = Map.empty[A, B]
  private var backward = Map.empty[B, A]

  override def +[B1 >: B](kv: (A, B1)): Map[A, B1] = {
    forward += kv._1 -> kv._2.asInstanceOf[B]
    backward += kv._2.asInstanceOf[B] -> kv._1
    this
  }

  override def get(key: A): Option[B] = forward.get(key)

  /**
    * Alias to `get` returns `Option[B]` for key `A`.
    */
  def getLeft(key: A): Option[B] = forward.get(key)

  /**
    * The reverse of `get` returns `Option[A]` for key `B`.
    */
  def getRight(key: B): Option[A] = backward.get(key)

  /**
    * Alias to `apply` returns `B` for key `A`.
    */
  def left(key: A): B = forward(key)

  /**
    * The reverse of `apply` returns `A` for key `B`.
    */
  def right(key: B): A = backward(key)

  override def iterator: Iterator[(A, B)] = forward.iterator

  override def -(key: A): Map[A, B] = {
    forward.get(key) match {
      case Some(value) => {
        forward -= key
        backward -= value
      }
      case None => // Not found
    }
    this
  }
}

object BiMap {
  def empty[A, B]: BiMap[A, B] = new BiMap[A, B]
}