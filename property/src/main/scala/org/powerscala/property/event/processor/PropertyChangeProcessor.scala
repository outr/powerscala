package org.powerscala.property.event.processor

import org.powerscala.hierarchy.event.AncestorProcessor
import org.powerscala.event.Listenable
import org.powerscala.property.event.PropertyChangeEvent
import org.powerscala.event.processor.UnitProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyChangeProcessor[T](implicit listenable: Listenable, eventManifest: Manifest[PropertyChangeEvent[T]])
      extends UnitProcessor[PropertyChangeEvent[T]]("change") with AncestorProcessor[PropertyChangeEvent[T], Unit, Unit]