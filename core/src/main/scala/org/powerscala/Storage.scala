package org.powerscala

import scala.collection.mutable

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Storage[V] {
  def get[T <: V](key: String) = Storage.get[T](this, key)
  def getOrSet[T <: V](key: String, value: => T) = Storage.getOrSet[T](this, key, value)
  def getAndSet[T <: V](key: String, value: T) = Storage.getAndSet[T](this, key, value)
  def getOrElse[T <: V](key: String, value: => T) = Storage.getOrElse[T](this, key, value)
  def apply[T <: V](key: String) = Storage[T](this, key)
  def update(key: String, value: Any) = Storage.set(this, key, value)
}

object Storage {
  private val map = new mutable.WeakHashMap[Any, Map[String, Any]]

  def get[T](obj: Any, key: String) = map.get(obj) match {
    case Some(m) => m.get(key).asInstanceOf[Option[T]]
    case None => None
  }
  def getOrSet[T](obj: Any, key: String, value: => T) = get[T](obj, key) match {
    case Some(v) => v
    case None => {
      set(obj, key, value)
      value
    }
  }
  def getAndSet[T](obj: Any, key: String, value: T) = {
    val previous = get[T](obj, key)
    set(obj, key, value)
    previous
  }
  def getOrElse[T](obj: Any, key: String, value: => T) = get[T](obj, key) match {
    case Some(v) => v
    case None => value
  }
  def apply[T](obj: Any, key: String) = get[T](obj, key).getOrElse(throw new NullPointerException(s"Unable to find $key"))
  def set(obj: Any, key: String, value: Any) = synchronized {
    val current = map.get(obj) match {
      case Some(m) => m
      case None => Map.empty[String, Any]
    }
    map.put(obj, current + (key -> value))
  }
}