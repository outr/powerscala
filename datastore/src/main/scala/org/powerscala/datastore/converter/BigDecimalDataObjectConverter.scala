package org.powerscala.datastore.converter

import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.DatastoreCollection

/**
 * @author Matt Hicks <matt@outr.com>
 */
object BigDecimalDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]) = {
    val d = db.get("value").asInstanceOf[Double]
    BigDecimal(d)
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val dbo = new BasicDBObject()
    dbo.put("class", obj.getClass.getName)
    dbo.put("value", obj.asInstanceOf[BigDecimal].doubleValue())
    dbo
  }
}
