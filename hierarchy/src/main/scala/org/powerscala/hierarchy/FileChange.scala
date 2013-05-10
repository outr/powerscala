package org.powerscala.hierarchy

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
sealed class FileChange extends EnumEntry

object FileChange extends Enumerated[FileChange] {
  val Created = new FileChange
  val Modified = new FileChange
  val Deleted = new FileChange
}