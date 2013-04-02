package org.powerscala.datastore

import event.{DatastoreDelete, DatastorePersist}
import java.util
import org.powerscala.event.Listenable
import org.powerscala.reflect._
import query._
import org.powerscala.hierarchy.Child
import query.DatastoreQuery
import query.FieldFilter
import util.UUID
import scala.Some
import collection.mutable.ListBuffer
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait DatastoreCollection[T <: Identifiable] extends Iterable[T] with Listenable with Child with Logging {
  def name: String
  def session: DatastoreSession
  def parent = session
  def manifest: Manifest[T]

  override def bus = session.bus

  protected var ids = Set.empty[util.UUID]

  def isPersisted(id: util.UUID): Boolean = ids.contains(id) match {
    case true => true
    case false => byId(id) match {
      case Some(v) => {
        ids += id
        true
      }
      case None => false
    }
  }

  def isPersisted(ref: T): Boolean = isPersisted(ref.id)

  /**
   * Make sure we don't run into infinite recursion
   */
  private val alreadyPersisted = new ThreadLocal[ListBuffer[T]]

  final def persist(refs: T*): Unit = {
    val wrapped = alreadyPersisted.get() != null
    if (!wrapped) {
      alreadyPersisted.set(ListBuffer.empty)
    }
    try {
      refs.foreach {
        case ref => persistInternal(ref)
      }
    } finally {
      if (!wrapped) {
        alreadyPersisted.set(null)
      }
    }
  }

  private def persistInternal(ref: T) = {
    if (alreadyPersisted.get().contains(ref)) {
      // Ignore already persisted
    } else {
      alreadyPersisted.get() += ref
      isPersisted(ref.id) match {
        case true => persistModified(ref)
        case false => persistNew(ref)
      }
      ids += ref.id
      fire(DatastorePersist(this, ref))
    }
  }

  final def delete(refs: T*): Unit = {
    refs.foreach {
      case ref => {
        deleteInternal(ref)
        ids -= ref.id
        fire(DatastoreDelete(this, ref))
      }
    }
  }

  def drop(): Unit

  final def byId(id: util.UUID) = query.filter(Field.id[T].equal(id)).headOption

  final def getById(id: util.UUID) = byId(id).getOrElse(throw new NullPointerException("Unable to find %s by id: %s".format(manifest.runtimeClass.getName, id)))

  final def byIds(ids: util.UUID*) = ids.map(id => byId(id)).flatten.toList

  /**
   * Reloads the passed instance from the datastore and returns it.
   *
   * @param t the instance to refresh
   * @return refreshed copy of t
   */
  final def refresh(t: T) = getById(t.id)

  def byExample(example: T) = {
    val ec = EnhancedClass(example.getClass)
    val method = ec.createMethod.getOrElse(throw new NullPointerException("%s is not a case class".format(example)))
    val companion = ec.companion.getOrElse(throw new NullPointerException("No companion found for %s".format(example)))
    val companionInstance = companion.instance.getOrElse(throw new NullPointerException("No companion instance found for %s".format(companion)))
    val defaults = method.args.collect {
      // Generate defaults excluding "id"
      case arg if (arg.name != "id") => arg.default(companionInstance) match {
        case None => arg.name -> arg.`type`.defaultForType // Default by the class type
        case Some(value) => arg.name -> value // Default argument for this case class
      }
    }.toMap
    var q = this.query
    ec.caseValues.foreach(cv => if (cv.name != "id" && defaults(cv.name) != cv[Any](example)) {
      val value = cv[Any](example)
      val field = Field.basic[T, Any](cv.name)
      q = q.filter(field equal value)
    })
    q
  }

  lazy val classField = new StringField[T]("class")

  def query = {
    val q = DatastoreQuery(collection = this)
    if (manifest.runtimeClass.isCase) {    // Auto-filter to case class
      q.filter(FieldFilter[T](classField, Operator.equal, manifest.runtimeClass.getName))
    } else {                          // Generalization, can't auto-filter
      q
    }
  }

  def executeQuery(query: DatastoreQuery[T]): Iterator[T]

  def executeQueryIds(query: DatastoreQuery[T]): Iterator[UUID]

  def executeQuerySize(query: DatastoreQuery[T]): Int

  /**
   * Updates the data to replace all entries with revision of 'revision' with the supplied newClass. This is useful for
   * revising a datastore programmatically.
   *
   * @param revision the revision to find and use the new class
   * @param newClass the new class to instantiate this revision with
   * @return the number of entries updated
   */
  def replaceRevisionClass(revision: Int, newClass: String): Int

  protected def persistNew(ref: T): Unit

  protected def persistModified(ref: T): Unit

  protected def deleteInternal(ref: T): Unit

  override def toString() = "%s[%s](%s)".format(getClass.getSimpleName, manifest.runtimeClass.getSimpleName, name)
}