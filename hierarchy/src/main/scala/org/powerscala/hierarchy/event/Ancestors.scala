package org.powerscala.hierarchy.event

import org.powerscala.event.ListenMode

/**
 * Ancestors mode is utilized to listen on a descendant (lower child) of a Listenable and still receive that event.
 *
 * @author Matt Hicks <matt@outr.com>
 */
object Ancestors extends ListenMode