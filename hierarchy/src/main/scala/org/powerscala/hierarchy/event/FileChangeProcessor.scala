package org.powerscala.hierarchy.event

import org.powerscala.event.processor.UnitProcessor
import org.powerscala.event.Listenable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class FileChangeProcessor(implicit listenable: Listenable) extends UnitProcessor[FileChangeEvent]("fileChange")