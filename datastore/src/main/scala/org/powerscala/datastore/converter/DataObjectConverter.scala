package org.powerscala.datastore.converter

import collection.mutable.ListBuffer

import java.util.{Calendar, UUID}
import com.mongodb.{BasicDBList, DBObject}
import org.powerscala.EnumEntry
import org.bson.types.ObjectId

import scala.collection.JavaConversions._
import org.powerscala.datastore.{DatastoreCollection, Lazy}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]): AnyRef

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]): DBObject
}

object DataObjectConverter {
  private var map = Map.empty[Class[_], DataObjectConverter]

  def fromDBObject[T](db: DBObject, collection: DatastoreCollection[_]) = {
    val clazz = Class.forName(db.get("class").toString)
    val converter = findConverter(clazz)
    converter.fromDBObject(db, collection).asInstanceOf[T]
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val converter = findConverter(obj.getClass)
    converter.toDBObject(obj, collection)
  }

  private def findConverter(clazz: Class[_]) = {
    if (!map.contains(clazz)) {
      synchronized {
        if (!map.contains(clazz)) {
          val converter = if (classOf[EnumEntry[_]].isAssignableFrom(clazz)) {
            EnumDataObjectConverter
          } else if (classOf[Calendar].isAssignableFrom(clazz)) {
            CalendarDataObjectConverter
          } else if (classOf[Map[_, _]].isAssignableFrom(clazz)) {
            MapDataObjectConverter
          } else if (classOf[java.util.Map[_, _]].isAssignableFrom(clazz)) {
            MapDataObjectConverter
          } else if (classOf[Lazy[_]].isAssignableFrom(clazz)) {
            LazyDataObjectConverter
          } else {
            new ReflectiveDataObjectConverter(clazz)
          }
          map += clazz -> converter
        }
      }
    }
    map(clazz)
  }

  def toDBValue(obj: Any, collection: DatastoreCollection[_]): Any = obj match {
    case objectId: ObjectId => objectId
    case uuid: UUID => uuid
    case seq: Seq[_] => toDBList(seq, collection)
    case array: Array[_] => toDBList(array, collection)
    case s: String => s
    case b: Boolean => b
    case b: Byte => b
    case c: Char => c
    case i: Int => i
    case l: Long => l
    case f: Float => f
    case d: Double => d
    case null => null
    case obj: AnyRef => toDBObject(obj, collection)
  }

  def fromDBValue(obj: Any, collection: DatastoreCollection[_]) = obj match {
    case objectId: ObjectId => objectId
    case uuid: UUID => {
      if (collection != null) {
        DatastoreCollection.assignId(collection, uuid)
      }
      uuid
    }
    case dbList: BasicDBList => fromDBList(dbList, collection)
    case s: String => s
    case b: Boolean => b
    case b: Byte => b
    case c: Char => c
    case i: Int => i
    case l: Long => l
    case f: Float => f
    case d: Double => d
    case null => null
    case obj: DBObject => fromDBObject(obj, collection)
  }

  private def toDBList(seq: Seq[_], collection: DatastoreCollection[_]): java.util.List[_] = {
    val dbList = new BasicDBList()
//    dbList.put("class", seq.getClass.getName)
    seq.foreach {
      case v => dbList.add(toDBValue(v, collection).asInstanceOf[AnyRef])
    }
    dbList
  }

  private def fromDBList(dbList: BasicDBList, collection: DatastoreCollection[_]): List[Any] = {
//    println("DBList Type: %s".format(dbList.get("class")))
    val buffer = new ListBuffer[Any]
    for (v <- dbList) {
      buffer += fromDBValue(v, collection)
    }
    buffer.toList
  }
}