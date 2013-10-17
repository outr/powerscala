package org.powerscala.property.event.processor

import org.powerscala.event.processor.ModifiableOptionProcessor
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyChangingProcessor[E](implicit listenable: Listenable, eventManifest: Manifest[E])
      extends ModifiableOptionProcessor[E]("changing")