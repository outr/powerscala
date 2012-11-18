package org.powerscala.datastore.converter

import scala.collection.JavaConversions._
import com.mongodb.{BasicDBObject, DBObject}
import org.powerscala.datastore.DatastoreCollection

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object MapDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]): AnyRef = {
    var map = Map.empty[Any, Any]
    val entries = db.get("count").toString.toInt
    val clazz: Class[_] = Class.forName(db.get("class").toString)
    for (index <- 0 until entries) {
      val obj = db.get("entry%s".format(index)).asInstanceOf[DBObject]
      val k = obj.get("key")
      val v = obj.get("value")
      val key = DataObjectConverter.fromDBValue(k, collection)
      val value = DataObjectConverter.fromDBValue(v, collection)
      map += key -> value
    }
    if (clazz.isAssignableFrom(classOf[Map[_, _]])) {
      map
    } else if (clazz == classOf[scala.collection.mutable.Map[_, _]]) {
      scala.collection.mutable.Map.empty[Any, Any] ++ map
    } else if (clazz.isAssignableFrom(classOf[java.util.Map[_, _]])) {
      val hashMap = new java.util.HashMap[Any, Any](entries)
      map.foreach(entry => hashMap.put(entry._1, entry._2))
      map
    } else {
      throw new RuntimeException("Unsupported map type: %s".format(clazz))
    }
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]): DBObject = {
    val dbo = new BasicDBObject()
    val map = obj match {
      case map: Map[_, _] => map
      case map: java.util.Map[_, _] => map.toMap
    }
    dbo.put("count", map.size)
    map.zipWithIndex.foreach(container => {
      val entry = container._1
      val index = container._2
      val key = DataObjectConverter.toDBValue(entry._1, collection)
      val value = DataObjectConverter.toDBValue(entry._2, collection)
      val entries = new BasicDBObject()
      entries.put("key", key)
      entries.put("value", value)
      dbo.put("entry%s".format(index), entries)
    })
    dbo.put("class", obj.getClass.getName)
    dbo
  }
}