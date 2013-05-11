package org.powerscala.hierarchy.event

import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ChildAddedProcessor
  extends UnitProcessor[ChildAddedEvent]
  with AncestorProcessor[ChildAddedEvent, Unit, Unit]
  with DescendantProcessor[ChildAddedEvent, Unit, Unit]