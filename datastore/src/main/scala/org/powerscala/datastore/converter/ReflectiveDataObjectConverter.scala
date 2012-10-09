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
      if (db.containsField(name)) {
        val value = db.get(name)
        val resultValue = (DataObjectConverter.fromDBValue(value, collection) match {
          case null if (cv.valueType.javaClass.isPrimitive) => cv.valueType.defaultForType
          case v if (cv.valueType.javaClass.isArray) => cv.valueType.javaClass.getComponentType.getName match {
            case "byte" => v.asInstanceOf[Seq[Int]].map(i => i.toByte).toArray
            case s => throw new RuntimeException("Unhandled value type for array: %s".format(s))
  //          val seq = v.asInstanceOf[Seq[Any]]
  //          val array = java.lang.reflect.Array.newInstance(cv.valueType.javaClass.getComponentType, seq.length)
  //          seq.zipWithIndex.foreach {
  //            case (entry, index) => java.lang.reflect.Array.set(array, index, entry)
  //          }
  //          array
              // TODO: add other type support
          }
          case v => v
        })
        Some(cv.name -> resultValue)
      } else {
        None
      }
    }).flatten.toMap
    try {
      clazz.create[AnyRef](values)
    } catch {
      case t: Throwable => throw new RuntimeException("Unable to instantiate %s with values %s".format(clazz, values), t)
    }
  }
}