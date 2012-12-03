package org.powerscala.datastore.impl.mongodb

import org.powerscala.datastore.{Identifiable, DatastoreSession}
import com.mongodb.Mongo

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class MongoDBDatastoreSession(val datastore: MongoDBDatastore) extends DatastoreSession {
  private var connected = false
  protected[mongodb] lazy val connection = {
    connected = true
    new Mongo(datastore.host, datastore.port)
  }
  protected[mongodb] lazy val database = connection.getDB(datastore.database)

  protected def createCollection[T <: Identifiable](name: String)(implicit manifest: Manifest[T]) = {
    new MongoDBDatastoreCollection[T](this, name)
  }

  def delete() = database.dropDatabase()

  def disconnect() = if (connected) {
    connection.close()
  }
}
