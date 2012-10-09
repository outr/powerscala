package org.powerscala.datastore.converter

import org.powerscala.datastore.{StaticLazyList, Identifiable, LazyList, DatastoreCollection}
import com.mongodb.{BasicDBList, BasicDBObject, DBObject}

import scala.collection.JavaConversions._
import java.util.UUID

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
object LazyListDataObjectConverter extends DataObjectConverter {
  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]) = {
    val ids = db.get("listIds").asInstanceOf[BasicDBList].toList.asInstanceOf[List[UUID]]
    val clazz = Class.forName(db.get("lazyClass").toString)
    val datastore = collection.session.datastore
    LazyList(ids, datastore, null)(Manifest.classType[Identifiable](clazz))
  }

  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val ll = obj.asInstanceOf[LazyList[Identifiable]]
    val values = ll() match {
      case null => Nil
      case vs => vs
    }
    val staticLazy = ll.isInstanceOf[StaticLazyList[_]]
    val lazyCollection = collection.session.collection()(ll.manifest)
    val dbo = new BasicDBObject()
    val array = new BasicDBList()
    values.foreach {
      case value => {
        if (staticLazy) {     // Only bother persisting if the reference has changed
          lazyCollection.persist(value)
        }
        array.add(value.id)
      }
    }
    dbo.put("listIds", array)
    dbo.put("class", classOf[LazyList[_]].getName)
    dbo.put("lazyClass", ll.manifest.erasure.getName)
    dbo
  }
}
