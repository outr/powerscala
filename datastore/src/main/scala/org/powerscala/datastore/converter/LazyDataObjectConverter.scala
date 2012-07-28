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
    if (db.containsField("lazy")) {
      val instance = DataObjectConverter.fromDBValue(db.get("lazy"), collection).asInstanceOf[Identifiable]
      Lazy(instance)
    } else {
      val lazyCollection = collection.session.collection()(Manifest.classType[Identifiable](clazz))
      Lazy(id, lazyCollection)
    }
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val l = obj.asInstanceOf[Lazy[Identifiable]]
    val instance = l()
    val dbo = new BasicDBObject()
    dbo.put("_id", l.id)
    dbo.put("class", classOf[Lazy[_]].getName)
    dbo.put("lazyClass", l.manifest.erasure.getName)
    if (collection != null && instance != null) {
      val lazyCollection = collection.session.collection()(l.manifest)
      lazyCollection.persist(l())
    } else {
      dbo.put("lazy", DataObjectConverter.toDBValue(instance, collection))
    }
    dbo
  }
}
