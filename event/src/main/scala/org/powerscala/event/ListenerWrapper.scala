package org.powerscala.event

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ListenerWrapper[E, V, R](modes: List[ListenMode], listener: Listener[E, V])(implicit val eventManifest: Manifest[E])