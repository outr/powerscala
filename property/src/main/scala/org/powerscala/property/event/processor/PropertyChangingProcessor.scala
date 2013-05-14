package org.powerscala.property.event.processor

import org.powerscala.event.processor.ModifiableProcessor
import org.powerscala.hierarchy.event.{DescendantProcessor, AncestorProcessor}
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class PropertyChangingProcessor[E](implicit listenable: Listenable, eventManifest: Manifest[E])
      extends ModifiableProcessor[E]("changing")
      with AncestorProcessor[E, Option[E], Option[E]]
      with DescendantProcessor[E, Option[E], Option[E]]