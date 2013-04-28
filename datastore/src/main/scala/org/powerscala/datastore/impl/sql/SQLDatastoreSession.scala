package org.powerscala.datastore.impl.sql

import org.powerscala.datastore.{Identifiable, DatastoreSession}

/**
 * @author Matt Hicks <matt@outr.com>
 */
class SQLDatastoreSession(val datastore: SQLDatastore) extends DatastoreSession {
  private var connected = false
  protected[sql] lazy val connection = {
    connected = true
    datastore.dataSource.getConnection
  }

  protected def createCollection[T <: Identifiable](name: String)(implicit manifest: Manifest[T]) = {
    new SQLDatastoreCollection[T](this, name)
  }

  def delete() = if (connected) {
    connection.close()
  }

  def disconnect() {}
}
