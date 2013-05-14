package org.powerscala.property.event.processor

import org.powerscala.hierarchy.event.StandardHierarchyEventProcessor
import org.powerscala.event.Listenable
import org.powerscala.property.event.PropertyChangeEvent

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyChangeProcessor[T](implicit listenable: Listenable, eventManifest: Manifest[PropertyChangeEvent[T]])
      extends StandardHierarchyEventProcessor[PropertyChangeEvent[T]]("change")