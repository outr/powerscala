package org.powerscala

import scala.collection.mutable

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Storage[K, V] {
  def get[T <: V](key: K): Option[T]
  def clear(): Unit
  protected def set(key: K, value: Option[Any]): Unit

  def remove(key: K) = set(key, None)
  def update(key: K, value: Any) = set(key, Some(value))
  def getOrSet[T <: V](key: K, value: => T): T = {
    get[T](key) match {
      case Some(v) => v
      case None => {
        val v = value
        update(key, v)
        v
      }
    }
  }
  def getAndSet[T <: V](key: K, value: T): Option[T] = {
    val previous = get[T](key)
    update(key, value)
    previous
  }
  def getOrElse[T <: V](key: K, value: => T): T = get[T](key) match {
    case Some(v) => v
    case None => value
  }
  def getAndRemove[T <: V](key: K): Option[T] = {
    val previous = get[T](key)
    remove(key)
    previous
  }
  def apply[T <: V](key: K): T = get[T](key).get
}

trait MappedStorage[K, V] extends Storage[K, V] {
  def get[T <: V](key: K) = Storage.get(this, key)
  def keyFromValue(value: Any) = Storage.keyFromValue(this, value)
  def map = Storage.map(this)
  def clear() = Storage.clear(this)

  protected def set(key: K, value: Option[Any]) = value match {
    case Some(v) => Storage.set(this, key, v)
    case None => Storage.remove(this, key)
  }
}

object Storage {
  private val _map = new mutable.WeakHashMap[Any, Map[Any, Any]]

  def map(obj: Any) = _map.getOrElse(obj, Map.empty[String, Any])
  def get[K, T](obj: Any, key: K) = _map.get(obj) match {
    case Some(m) => m.get(key).asInstanceOf[Option[T]]
    case None => None
  }
  def getOrSet[K, T](obj: Any, key: K, value: => T) = get[K, T](obj, key) match {
    case Some(v) => v
    case None => {
      val t: T = value
      set(obj, key, t)
      t
    }
  }
  def getAndSet[K, T](obj: Any, key: K, value: T) = {
    val previous = get(obj, key)
    set(obj, key, value)
    previous
  }
  def getOrElse[K, T](obj: Any, key: K, value: => T) = get(obj, key) match {
    case Some(v) => v
    case None => value
  }
  def getAndRemove[K, T](obj: Any, key: K) = {
    val option = get(obj, key)
    remove(obj, key)
    option
  }
  def remove[K](obj: Any, key: K) = synchronized {
    _map.get(obj) match {
      case Some(m) if m.contains(key) => {
        _map.put(obj, m - key)
        true
      }
      case _ => false
    }
  }
  def keyFromValue(obj: Any, value: Any) = _map.get(obj) match {
    case Some(m) => m.find(t => t._2 == value).map(t => t._1)
    case None => None
  }
  def clear(obj: Any) = synchronized {
    _map.put(obj, Map.empty[Any, Any])
  }
  def apply[T](obj: Any, key: String) = get(obj, key).getOrElse(throw new NullPointerException(s"Unable to find $key"))
  def set[K, T](obj: Any, key: K, value: T) = synchronized {
    val current = _map.get(obj) match {
      case Some(m) => m
      case None => Map.empty[Any, Any]
    }
    _map.put(obj, current + (key -> value))
  }
}