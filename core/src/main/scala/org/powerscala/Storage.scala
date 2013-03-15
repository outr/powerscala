package org.powerscala

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Storage[V] {
  private var _store: Map[String, Any] = _
  def get[T <: V](key: String) = if (_store != null) {
    _store.get(key).asInstanceOf[Option[T]]
  } else {
    None
  }
  def getOrSet[T <: V](key: String, value: => T) = get[T](key) match {
    case Some(v) => v
    case None => {
      update(key, value)
      value
    }
  }
  def getAndSet[T <: V](key: String, value: T) = synchronized {
    val previous = get[T](key)
    update(key, value)
    previous
  }
  def getOrElse[T <: V](key: String, value: => T) = get[T](key) match {
    case Some(v) => v
    case None => value
  }
  def apply[T <: V](key: String) = if (_store != null) {
    _store(key).asInstanceOf[T]
  } else {
    null.asInstanceOf[T]
  }
  def update(key: String, value: Any) = synchronized {
    if (_store == null) {
      _store = Map.empty
    }
    _store += key -> value
  }
}
