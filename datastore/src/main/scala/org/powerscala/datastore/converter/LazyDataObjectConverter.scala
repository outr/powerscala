package org.powerscala.datastore.converter

import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.{Identifiable, Lazy, DatastoreCollection}
import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object LazyDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]): AnyRef = {
    val id = db.get("_id").asInstanceOf[util.UUID]
    val clazz = Class.forName(db.get("lazyClass").toString)
      val datastore = collection.session.datastore
      val name: String = null   // Support specially named collections for lazy?
      Lazy(id, datastore, name)(Manifest.classType[Identifiable](clazz))
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val l = obj.asInstanceOf[Lazy[Identifiable]]
    val instance = l()
    if (instance != null) {
      val dbo = new BasicDBObject()
      dbo.put("_id", l.id)
      dbo.put("class", classOf[Lazy[_]].getName)
      dbo.put("lazyClass", l.manifest.erasure.getName)
      val lazyCollection = collection.session.collection()(l.manifest)
      lazyCollection.persist(instance)
      dbo
    } else {
      null
    }
  }
}
