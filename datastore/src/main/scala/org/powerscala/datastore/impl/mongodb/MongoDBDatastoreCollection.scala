package org.powerscala.datastore.impl.mongodb

import org.powerscala.datastore._
import org.powerscala.datastore.converter.DataObjectConverter
import com.mongodb.BasicDBObject

import query.{Filter, SortDirection, Operator, DatastoreQuery}
import scala.collection.JavaConversions._

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class MongoDBDatastoreCollection[T <: Identifiable](val session: MongoDBDatastoreSession, val name: String)
                                                   extends DatastoreCollection[T] {
  lazy val collection = session.database.getCollection(name)

  protected def persistNew(ref: T) = collection.insert(DataObjectConverter.toDBObject(ref, this))

  protected def persistModified(ref: T) = {
    collection.findAndModify(new BasicDBObject("_id", ref.id), DataObjectConverter.toDBObject(ref, this))
  }

  protected def deleteInternal(ref: T) = {
    collection.findAndRemove(new BasicDBObject("_id", ref.id))
  }

  private def filter2Name(filter: Filter[_]): String = filter.operator match {
    case Operator.subfilter => "%s.%s".format(filter.field.name, filter2Name(filter.value.asInstanceOf[Filter[_]]))
    case _ => filter.field.name
  }

  private def filter2DBValue(filter: Filter[_]): Any = filter.operator match {
      case Operator.< => new BasicDBObject("$lt", DataObjectConverter.toDBValue(filter.value, this))
      case Operator.<= => new BasicDBObject("$lte", DataObjectConverter.toDBValue(filter.value, this))
      case Operator.> => new BasicDBObject("$gt", DataObjectConverter.toDBValue(filter.value, this))
      case Operator.>= => new BasicDBObject("$gte", DataObjectConverter.toDBValue(filter.value, this))
      case Operator.equal => DataObjectConverter.toDBValue(filter.value, this)
      case Operator.nequal => new BasicDBObject("$ne", DataObjectConverter.toDBValue(filter.value, this))
      case Operator.regex => new BasicDBObject("$regex", filter.value)
      case Operator.subfilter => filter2DBValue(filter.value.asInstanceOf[Filter[_]])
  }

  def executeQuery(query: DatastoreQuery[T]) = {
    val dbo = new BasicDBObject()
    val filters = query._filters.reverse
    filters.foreach {
      case filter => dbo.put(filter2Name(filter), filter2DBValue(filter))
    }
    var cursor = collection.find(dbo)
    if (query._skip > 0) {
      cursor = cursor.skip(query._skip)
    }
    if (query._limit > 0) {
      cursor = cursor.limit(query._limit)
    }
    val sort = query._sort.reverse
    if (sort.nonEmpty) {
      val sdbo = new BasicDBObject()
      sort.foreach {
        case s => {
          val direction = s.direction match {
            case SortDirection.Ascending => 1
            case SortDirection.Descending => -1
          }
          sdbo.put(s.field.name, direction)
        }
      }
      cursor = cursor.sort(sdbo)
    }
    asScalaIterator(cursor).map(entry => {
      val v = DataObjectConverter.fromDBValue(entry, this)
      v match {
        case identifiable: Identifiable => ids += identifiable.id
        case _ =>
      }
      v
    }).asInstanceOf[Iterator[T]]
  }

  def iterator = asScalaIterator(collection.find()).map(entry => DataObjectConverter.fromDBValue(entry, this)).asInstanceOf[Iterator[T]]
}
