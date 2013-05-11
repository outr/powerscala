package org.powerscala.hierarchy.event

import org.powerscala.event.ListenMode

/**
 * Descendants mode is utilized to listen on an ancestor (higher parent) of a Listenable and still receive that event.
 *
 * @author Matt Hicks <matt@outr.com>
 */
object Descendants extends ListenMode