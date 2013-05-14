package org.powerscala.hierarchy.event

import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ChildAddedProcessor(implicit listenable: Listenable) extends StandardHierarchyEventProcessor[ChildAddedEvent]("childAdded")