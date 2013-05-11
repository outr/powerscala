package org.powerscala.event

import org.powerscala.event.processor.EventProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ListenerWrapper[E, V, R](modes: List[ListenMode], processor: EventProcessor[E, V, R], listener: Listener[E, V])