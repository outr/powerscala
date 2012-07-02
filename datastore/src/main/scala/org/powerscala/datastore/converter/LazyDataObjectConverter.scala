package org.powerscala.datastore.converter

import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.{Persistable, Lazy, DatastoreCollection}
import java.util

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object LazyDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]): AnyRef = {
    val id = db.get("_id").asInstanceOf[util.UUID]
    val clazz = Class.forName(db.get("lazyClass").toString)
    if (db.containsField("lazy")) {
      val instance = DataObjectConverter.fromDBValue(db.get("lazy"), collection).asInstanceOf[Persistable]
      Lazy(instance)
    } else {
      val lazyCollection = collection.session.collection()(Manifest.classType[Persistable](clazz))
      Lazy(id, lazyCollection)
    }
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val l = obj.asInstanceOf[Lazy[Persistable]]
    val instance = l()
    val clazz = instance.getClass
    val dbo = new BasicDBObject()
    dbo.put("_id", l.id)
    dbo.put("class", classOf[Lazy[_]].getName)
    dbo.put("lazyClass", clazz.getName)
    if (collection != null) {
      val lazyCollection = collection.session.collection()(Manifest.classType[Persistable](instance.getClass))
      lazyCollection.persist(l())
    } else {
      dbo.put("lazy", DataObjectConverter.toDBValue(instance, collection))
    }
    dbo
  }
}
