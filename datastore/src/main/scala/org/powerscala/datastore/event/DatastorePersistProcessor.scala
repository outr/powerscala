package org.powerscala.datastore.event

import org.powerscala.datastore.Identifiable
import org.powerscala.hierarchy.event.StandardHierarchyEventProcessor
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class DatastorePersistProcessor(implicit listenable: Listenable) extends StandardHierarchyEventProcessor[DatastorePersist[_ <: Identifiable]]("persist")