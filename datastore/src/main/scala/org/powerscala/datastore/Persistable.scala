package org.powerscala.datastore

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Persistable extends Identifiable {
  protected[datastore] var _persistanceState: PersistenceState = PersistenceState.NotPersisted

  def persistenceState = _persistanceState
}

sealed class PersistenceState

object PersistenceState {
  val NotPersisted = new PersistenceState
  val Persisted = new PersistenceState
}