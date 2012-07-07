package org.powerscala.datastore.impl.mongodb

import org.powerscala.datastore.Datastore

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
class MongoDBDatastore(val host: String = "localhost", val port: Int = 27017, val database: String = "datastore") extends Datastore {
  override def session = super.session.asInstanceOf[MongoDBDatastoreSession]

  /**
   * If set to true, all Identifiable objects will be be cross-persisted to their own collection when persisted as part
   * of another collection.
   *
   * For example, if Person class had a reference to Address and Address is Identifiable, then Address will also be
   * persisted to the Address collection if globalize is set to true.
   */
  // TODO: support
  var globalize = false

  protected def createSession() = new MongoDBDatastoreSession(this)
}
