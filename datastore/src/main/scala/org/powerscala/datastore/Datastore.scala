package org.powerscala.datastore

import org.powerscala.event.Listenable
import org.powerscala.hierarchy.Child
import org.powerscala.bus.Bus
import org.powerscala.datastore.query.Field
import org.powerscala.reflect.EnhancedClass
import org.powerscala.LocalStack

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Datastore extends Listenable with Child {
  override def bus = Bus()

  private val sessions = new ThreadLocal[DatastoreSession]

  /**
   * Indexes that need to be defined for this Datastore.
   */
  def indexes = List.empty[Field[_, _]]

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

  private var _aliases = List.empty[Class[_] => Option[String]]

  /**
   * Allows overriding the collection name being utilized for the datastore.
   *
   * Defaults to return the same value passed in or the simple class name if name is null.
   */
  final def aliasName(name: String, clazz: Class[_]) = {
    val alias = name match {
      case null => collectionNameForClass(clazz)
      case _ => name
    }
    alias
  }

  def alias(f: Class[_] => Option[String]) = synchronized {
    _aliases = (f :: _aliases.reverse).reverse
  }

  def collectionNameForClass(clazz: Class[_]) = _aliases.view.map(f => f(clazz)).flatten.headOption match {
    case Some(s) => s
    case None => clazz.getSimpleName
  }

  /**
   * Creates an alias for all classes that are subclasses of clazz to map to the clazz.getSimpleName.
   *
   * @param clazz
   */
  def register(clazz: Class[_]) = {
    alias {
      case c => if (clazz.isAssignableFrom(c)) {
        Some(clazz.getSimpleName)
      } else {
        None
      }
    }
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

  private var initialized = Set.empty[Class[_]]

  def creatingCollection[T <: Identifiable](name: String, session: DatastoreSession)(implicit manifest: Manifest[T]) = synchronized {
  }

  def createdCollection[T <: Identifiable](name: String, session: DatastoreSession, collection: DatastoreCollection[T])
                          (implicit manifest: Manifest[T]) = synchronized {
    if (!initialized.contains(manifest.runtimeClass)) {
      initializeCollection[T](name, session, collection, manifest.runtimeClass)
      initialized += manifest.runtimeClass
    }
  }

  def initializeCollection[T <: Identifiable](name: String, session: DatastoreSession, collection: DatastoreCollection[T], clazz: Class[_]) = {
    val fields = indexes.collect {
      case f if (f.manifest.runtimeClass == clazz) => f.asInstanceOf[Field[T, _]]
    }
    collection.createIndexes(fields)
  }

  /**
   * Called by DatastoreSession to create an instance of a DatastoreCollection.
   *
   * By default this method simply invokes: creator(name)
   *
   * @param name the name of this collection
   * @param session the current session the collection is to be created with
   * @param creator the default creator function
   * @tparam T the type of collection to be created
   * @return DatastoreCollection[T]
   */
  def createCollection[T <: Identifiable](name: String,
                                          session: DatastoreSession,
                                          creator: String => DatastoreCollection[T])
                                         (implicit manifest: Manifest[T]) = creator(name)

  def disconnect() = {
    session.disconnect()
    sessions.set(null)
  }

  /**
   * @return a new session for use with this datastore
   */
  protected def createSession(): DatastoreSession

  protected[datastore] val convertValues = new LocalStack[Map[String, Any]]
  protected[datastore] val convertFunction = new ConvertFunction

  class ConvertFunction extends ((String, Any, EnhancedClass) => Any) {
    def apply(name: String, value: Any, resultType: EnhancedClass) = convertInternal(name, value, resultType)
  }

  private def convertInternal(name: String, value: Any, resultType: EnhancedClass): Any = {
    val values = convertValues()
    convert(name, value, resultType, values)
  }

  /**
   * Called during deserialization from the datastore to support conversion from one type (what is persisted in the datastore)
   * to another type (what is represented in the class structure). This can be useful for refactors of the class
   * structure without having to immediately update the datastore.
   *
   * @param name the name of the field for this value being converted
   * @param value the value found in the datastore
   * @param resultType the expected result type
   * @param values the other values for the other fields for this conversion
   * @return value converted to resultType
   */
  def convert(name: String, value: Any, resultType: EnhancedClass, values: Map[String, Any]): Any = throw new NullPointerException(s"Datastore cannot convert $name with value of $value (${value.getClass}) to $resultType. Extend Datastore.convert to support this conversion.")
}