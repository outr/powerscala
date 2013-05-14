package org.powerscala.property.event.processor

import org.powerscala.hierarchy.event.StandardHierarchyEventProcessor
import org.powerscala.event.Listenable
import org.powerscala.property.event.PropertyRead

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyReadProcessor(implicit listenable: Listenable) extends StandardHierarchyEventProcessor[PropertyRead]("read")