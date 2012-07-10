package org.powerscala.datastore.impl.mongodb

import org.powerscala.datastore._
import org.powerscala.datastore.converter.DataObjectConverter
import com.mongodb.{BasicDBList, BasicDBObject}

import query._
import query.DatastoreQuery
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

  override def size = collection.count().toInt

  private def filter2Name(filter: Filter[_]): String = filter match {
    case ff: FieldFilter[_] => ff.operator match {
      case Operator.subfilter => "%s.%s".format(ff.field.name, filter2Name(ff.value.asInstanceOf[Filter[_]]))
      case _ => ff.field.name
    }
    case of: OrFilter[_] => "$or"   // TODO: fix 'or' and 'and' support
    case af: AndFilter[_] => "$and"
    case _ => throw new RuntimeException("Unknown filter type: %s".format(filter.getClass.getName))
  }

  private def filter2DBValue(filter: Filter[_]): Any = filter match {
    case ff: FieldFilter[_] => {
      ff.operator match {
        case Operator.< => new BasicDBObject("$lt", DataObjectConverter.toDBValue(ff.value, this))
        case Operator.<= => new BasicDBObject("$lte", DataObjectConverter.toDBValue(ff.value, this))
        case Operator.> => new BasicDBObject("$gt", DataObjectConverter.toDBValue(ff.value, this))
        case Operator.>= => new BasicDBObject("$gte", DataObjectConverter.toDBValue(ff.value, this))
        case Operator.equal => DataObjectConverter.toDBValue(ff.value, this)
        case Operator.nequal => new BasicDBObject("$ne", DataObjectConverter.toDBValue(ff.value, this))
        case Operator.regex => new BasicDBObject("$regex", ff.value)
        case Operator.in => {
          val list = new BasicDBList()
          ff.value.asInstanceOf[Seq[_]].foreach {
            case v => list.add(DataObjectConverter.toDBValue(v, this).asInstanceOf[AnyRef])
          }
          new BasicDBObject("$in", list)
        }
        case Operator.subfilter => filter2DBValue(ff.value.asInstanceOf[Filter[_]])
      }
    }
    case sf: SubFilter[_] => {
      val list = new BasicDBList()
      sf.filters.foreach {
        case subFilter => list.add(filter2DBValue(subFilter).asInstanceOf[AnyRef])
      }
      list
    }
    case _ => throw new RuntimeException("Unknown filter type: %s".format(filter.getClass.getName))
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
