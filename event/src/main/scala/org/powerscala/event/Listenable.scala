package org.powerscala.event

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait Listenable {
  implicit val thisListenable = this

  val listeners = new Listeners
}