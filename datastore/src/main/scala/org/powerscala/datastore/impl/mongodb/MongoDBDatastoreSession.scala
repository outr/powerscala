package org.powerscala.datastore.impl.mongodb

import org.powerscala.datastore.{Identifiable, DatastoreSession}
import com.mongodb.Mongo

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class MongoDBDatastoreSession(val datastore: MongoDBDatastore) extends DatastoreSession {
  protected[mongodb] val connection = new Mongo(datastore.host, datastore.port)
  protected[mongodb] val database = connection.getDB(datastore.database)

  protected def createCollection[T <: Identifiable](name: String)(implicit manifest: Manifest[T]) = {
    new MongoDBDatastoreCollection[T](this, name)
  }

  def dropDatabase() = database.dropDatabase()

  def disconnect() = {
    connection.close()
  }
}
