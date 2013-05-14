package org.powerscala.hierarchy.event

import org.powerscala.event.processor.UnitProcessor
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class StandardHierarchyEventProcessor[E](name: String)(implicit listenable: Listenable, eventManifest: Manifest[E])
  extends UnitProcessor[E](name)
  with AncestorProcessor[E, Unit, Unit]
  with DescendantProcessor[E, Unit, Unit]