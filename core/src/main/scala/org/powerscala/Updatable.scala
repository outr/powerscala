package org.powerscala

/**
 * Updatable represents a class that gets updated with the delta between the last update and the
 * current update.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Updatable {
  /**
   * Invoked with the amount of time that has elapsed since the last call of this method in seconds.
   */
  def update(delta: Double) = {
  }
}