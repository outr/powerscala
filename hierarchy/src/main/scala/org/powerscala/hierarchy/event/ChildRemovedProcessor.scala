package org.powerscala.hierarchy.event

import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ChildRemovedProcessor[Child](implicit listenable: Listenable, manifest: Manifest[Child]) extends StandardHierarchyEventProcessor[ChildRemovedEvent[Child]]("childRemoved")