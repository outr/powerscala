package org.powerscala.datastore.converter

import org.powerscala.{EnumEntry, Enumerated}
import org.powerscala.reflect.EnhancedClass
import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.DatastoreCollection

/**
 * Processes EnumEntry's
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object EnumDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]) = {
    val clazz: EnhancedClass = Class.forName(db.get("class").toString)
    val companion = clazz.companion.getOrElse(throw new RuntimeException("No companion for %s".format(clazz))).instance.get
    companion.asInstanceOf[Enumerated[_]](db.get("name").toString).asInstanceOf[AnyRef]
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val dbo = new BasicDBObject()
    dbo.put("class", obj.getClass.getName)
    dbo.put("name", obj.asInstanceOf[EnumEntry[_]].name())
    dbo
  }
}