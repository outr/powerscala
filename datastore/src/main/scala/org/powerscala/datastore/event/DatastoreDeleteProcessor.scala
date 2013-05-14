package org.powerscala.datastore.event

import org.powerscala.hierarchy.event.StandardHierarchyEventProcessor
import org.powerscala.datastore.Identifiable
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class DatastoreDeleteProcessor(implicit listenable: Listenable) extends StandardHierarchyEventProcessor[DatastoreDelete[_ <: Identifiable]]("delete")