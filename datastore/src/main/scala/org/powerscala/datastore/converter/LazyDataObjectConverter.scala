package org.powerscala.datastore.converter

import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.{StaticLazy, Identifiable, Lazy, DatastoreCollection}
import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object LazyDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]): AnyRef = {
    val id = db.get("_id").asInstanceOf[util.UUID]
    val clazz = Class.forName(db.get("lazyClass").toString)
    val datastore = collection.session.datastore
    val name: String = null // Support specially named collections for lazy?
    val manifest = Manifest.classType[Identifiable](clazz)
    if (id == null) {
      Lazy(null.asInstanceOf[Identifiable])(manifest)
    } else {
      Lazy(id, datastore, name)(manifest)
    }
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val l = obj.asInstanceOf[Lazy[Identifiable]]
    val instance = l()
    val dbo = new BasicDBObject()
    val id = l match {
      case null => null
      case _ => l.id
    }
    dbo.put("_id", id)
    dbo.put("class", classOf[Lazy[_]].getName)
    dbo.put("lazyClass", l.manifest.erasure.getName)
    val lazyCollection = collection.session.collection()(l.manifest)
    if (instance != null && l.isInstanceOf[StaticLazy[_]]) {    // Only persist if the value has changed
      lazyCollection.persist(instance)
    }
    dbo
  }
}
