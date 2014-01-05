package org.powerscala.datastore.impl.mongodb

import org.powerscala.datastore.{Identifiable, DatastoreSession}

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class MongoDBDatastoreSession(val datastore: MongoDBDatastore) extends DatastoreSession {
  protected[mongodb] lazy val database = datastore.connection.getDB(datastore.database)

  protected def createCollection[T <: Identifiable](name: String)(implicit manifest: Manifest[T]) = {
    new MongoDBDatastoreCollection[T](this, name)
  }

  def delete() = database.dropDatabase()

  def disconnect() = {}
}
