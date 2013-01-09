package org.powerscala.hierarchy.event

import org.powerscala.event.Event
import java.io.File
import org.powerscala.hierarchy.FileChange

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
case class FileChangeEvent(file: File, change: FileChange) extends Event