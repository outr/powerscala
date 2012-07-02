package org.powerscala.datastore

import java.util.UUID

/**
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Identifiable {
  /**
   * @return universally unique identifier for this instance
   */
  def id: UUID
}
