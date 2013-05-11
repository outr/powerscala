package org.powerscala.hierarchy.event

import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
object ChildRemovedProcessor
  extends UnitProcessor[ChildRemovedEvent]
  with AncestorProcessor[ChildRemovedEvent, Unit, Unit]
  with DescendantProcessor[ChildRemovedEvent, Unit, Unit]