package org.powerscala.datastore.converter

import org.powerscala.reflect.{CaseValue, EnhancedClass}
import com.mongodb.{DBObject, BasicDBObject}
import org.powerscala.datastore.DatastoreCollection

class ReflectiveDataObjectConverter(erasure: EnhancedClass) extends DataObjectConverter {
  def toDBObject(obj: AnyRef, collection: DatastoreCollection[_]) = {
    val dbo = new BasicDBObject()
    val clazz: EnhancedClass = obj.getClass
    if (clazz.isCase) {
      val cv2Builder = (cv: CaseValue) => {
        if (!cv.valueType.isTransient) {
          val name = cv.name match {
            case "id" => "_id"
            case s => s
          }
          val value = DataObjectConverter.toDBValue(cv.apply(obj), collection)
          dbo.put(name, value)
        }
      }
      clazz.caseValues.foreach(cv2Builder)
    } else {
      throw new RuntimeException("Only case classes are supported: %s".format(clazz))
    }
    dbo.put("class", clazz.name)
    dbo
  }

  def fromDBObject(db: DBObject, collection: DatastoreCollection[_]): AnyRef = {
    val clazz: EnhancedClass = Class.forName(db.get("class").toString)
    val values = clazz.caseValues.map(cv => {
      val name = cv.name match {
        case "id" => "_id"
        case s => s
      }
      val value = db.get(name)
      cv.name -> DataObjectConverter.fromDBValue(value, collection)
    }).toMap
    try {
      clazz.create[AnyRef](values)
    } catch {
      case exc => throw new RuntimeException("Unable to instantiate %s with values %s".format(clazz, values), exc)
    }
  }
}