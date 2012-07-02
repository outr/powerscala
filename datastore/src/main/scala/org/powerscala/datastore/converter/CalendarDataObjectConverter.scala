package org.powerscala.datastore.converter

import java.util.Calendar
import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.DatastoreCollection

/**
 * @author Matt Hicks <matt@outr.com>
 */
object CalendarDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]) = {
    val c = Calendar.getInstance()
    c.setTimeInMillis(db.get("time").toString.toLong)
    c
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val dbo = new BasicDBObject()
    dbo.put("class", obj.getClass.getName)
    dbo.put("time", obj.asInstanceOf[Calendar].getTimeInMillis.toString)
    dbo
  }
}
