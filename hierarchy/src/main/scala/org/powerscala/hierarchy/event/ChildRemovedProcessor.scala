package org.powerscala.hierarchy.event

import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ChildRemovedProcessor(implicit listenable: Listenable) extends StandardHierarchyEventProcessor[ChildRemovedEvent]