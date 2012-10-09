package org.powerscala.datastore

import org.powerscala.event.Listenable
import org.powerscala.hierarchy.Child
import org.powerscala.bus.Bus

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Datastore extends Listenable with Child {
  override def bus = Bus()

  private val sessions = new ThreadLocal[DatastoreSession]

  def parent = null

  /**
   * @return the existing session for this thread or null if one does not exist.
   */
  def session = sessions.get()

  /**
   * Executes the supplied function within a local session. If a session already exists it will be utilized or a new one
   * will be created and terminated upon completion of the session block.
   *
   * @param f the function to execute within the session
   * @return the result from the function
   */
  def apply[T](f: DatastoreSession => T): T = {
    val created = createSessionForThread()
    try {
      f(session)
    } finally {
      if (created) {
        disconnect()
      }
    }
  }

  /**
   * Allows overriding the collection name being utilized for the datastore.
   *
   * Defaults to return the same value passed in or the simple class name if name is null.
   */
  def aliasName(name: String, clazz: Class[_]) = name match {
    case null => clazz.getSimpleName
    case _ => name
  }

  def collection[T <: Identifiable, R](f: DatastoreCollection[T] => R)(implicit manifest: Manifest[T]): R = {
    apply {
      case session => {
        val c = session.collection[T](null)(manifest)
        f(c)
      }
    }
  }

  def createSessionForThread() = {
    session match {
      case null => {
        val s = createSession()
        sessions.set(s)
        true
      }
      case s => false
    }
  }

  def disconnect() = {
    session.disconnect()
    sessions.set(null)
  }

  /**
   * @return a new session for use with this datastore
   */
  protected def createSession(): DatastoreSession
}