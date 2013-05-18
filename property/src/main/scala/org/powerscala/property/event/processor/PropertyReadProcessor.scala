package org.powerscala.property.event.processor

import org.powerscala.event.Listenable
import org.powerscala.property.event.PropertyRead
import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyReadProcessor(implicit listenable: Listenable) extends UnitProcessor[PropertyRead]("read")