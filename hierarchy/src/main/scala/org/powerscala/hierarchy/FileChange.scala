package org.powerscala.hierarchy

import org.powerscala.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
sealed class FileChange extends EnumEntry[FileChange]

object FileChange extends Enumerated[FileChange] {
  val Created = new FileChange
  val Modified = new FileChange
  val Deleted = new FileChange
}