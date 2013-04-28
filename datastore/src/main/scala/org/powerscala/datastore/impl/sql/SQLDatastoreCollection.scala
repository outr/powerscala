package org.powerscala.datastore.impl.sql

import org.powerscala.datastore.{Identifiable, DatastoreCollection}
import org.powerscala.datastore.query.{Field, DatastoreQuery}
import java.util.UUID

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SQLDatastoreCollection[T <: Identifiable](val session: SQLDatastoreSession, val name: String)
                                               (implicit val manifest: Manifest[T])
                                                extends DatastoreCollection[T] {
  lazy val connection = session.connection

  protected def persistNew(ref: T) = {
    // TODO: implement
  }

  protected def persistModified(ref: T) = {
    // TODO: implement
  }

  protected def deleteInternal(ref: T) = {
    // TODO: implement
  }

  def drop() = {
    // TODO: implement
  }

  def createIndexes(fields: List[Field[T, _]]) {}

  def executeQuery(query: DatastoreQuery[T]): Iterator[T] = ???

  def executeQueryIds(query: DatastoreQuery[T]): Iterator[UUID] = ???

  def executeQuerySize(query: DatastoreQuery[T]): Int = ???

  /**
   * Updates the data to replace all entries with revision of 'revision' with the supplied newClass. This is useful for
   * revising a datastore programmatically.
   *
   * @param revision the revision to find and use the new class
   * @param newClass the new class to instantiate this revision with
   * @return the number of entries updated
   */
  def replaceRevisionClass(revision: Int, newClass: String): Int = ???
}
