package org.powerscala

/**
 * Finishable simply defines a method "isFinished" that returns true if the instance is complete.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Finishable {
  def isFinished: Boolean
}