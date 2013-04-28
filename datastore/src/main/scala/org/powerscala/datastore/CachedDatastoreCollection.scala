package org.powerscala.datastore

import org.powerscala.datastore.query.{Field, DatastoreQuery}
import java.util.UUID

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class CachedDatastoreCollection[T <: Identifiable](collection: DatastoreCollection[T]) extends DatastoreCollection[T] {
  def name = collection.name
  def session = collection.session
  def manifest = collection.manifest

  lazy val cache = CachedDatastoreCollection(name, collection)

  def drop() = cache.drop(collection)

  def createIndexes(fields: List[Field[T, _]]) = collection.createIndexes(fields)

  override def byId(id: UUID) = cache.byId(id, collection)

  def executeQuery(query: DatastoreQuery[T]) = collection.executeQuery(query) // cache.executeQuery(query, collection)

  def executeQueryIds(query: DatastoreQuery[T]) = collection.executeQueryIds(query) // cache.executeQueryIds(query, collection)

  def executeQuerySize(query: DatastoreQuery[T]) = collection.executeQuerySize(query) // cache.executeQuerySize(query, collection)

  def replaceRevisionClass(revision: Int, newClass: String) = collection.replaceRevisionClass(revision, newClass) // cache.replaceRevisionClass(revision, newClass, collection)

  protected def persistNew(ref: T) = cache.persistNew(ref, collection)

  protected def persistModified(ref: T) = cache.persistModified(ref, collection)

  protected def deleteInternal(ref: T) = cache.deleteInternal(ref, collection)
}

object CachedDatastoreCollection {
  private var cache = Map.empty[String, CollectionCache[_]]

  def apply[T <: Identifiable](name: String, collection: DatastoreCollection[T]) = synchronized {
    cache.get(name) match {
      case Some(cc) => cc.asInstanceOf[CollectionCache[T]]
      case None => {
        val cc = new CollectionCache[T]
        cc.refresh(collection)
        cache += name -> cc
        cc
      }
    }
  }
}

class CollectionCache[T <: Identifiable] {
  private var list = List.empty[T]
  private var map = Map.empty[UUID, T]

  def byId(id: UUID, collection: DatastoreCollection[T]) = map.get(id)

  def iterator(collection: DatastoreCollection[T]) = list.iterator

//  def executeQuery(query: DatastoreQuery[T], collection: DatastoreCollection[T]) =

  def refresh(collection: DatastoreCollection[T]) = synchronized {    // Preload cache
    var _list = List.empty[T]
    var _map = Map.empty[UUID, T]
    collection.foreach {
      case t => {
        _list = t :: _list
        _map += t.id -> t
      }
    }
    list = _list.reverse
    map = _map
  }

  def persistNew(ref: T, collection: DatastoreCollection[T]) = synchronized {
    collection.persist(ref)
    list = (ref :: list.reverse).reverse
    map += ref.id -> ref
  }

  def persistModified(ref: T, collection: DatastoreCollection[T]) = synchronized {
    collection.persist(ref)
    list = list.map(t => if (t.id == ref.id) ref else t)
    map += ref.id -> ref
  }

  def deleteInternal(ref: T, collection: DatastoreCollection[T]) = synchronized {
    collection.delete(ref)
    list = list.filterNot(t => t.id == ref.id)
    map -= ref.id
  }

  def drop(collection: DatastoreCollection[T]) = synchronized {
    collection.drop()
    list = Nil
    map = Map.empty
  }
}