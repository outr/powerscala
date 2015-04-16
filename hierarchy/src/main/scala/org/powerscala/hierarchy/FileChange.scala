package org.powerscala.hierarchy

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
sealed trait FileChange extends EnumEntry

object FileChange extends Enumerated[FileChange] {
  case object Created extends FileChange
  case object Modified extends FileChange
  case object Deleted extends FileChange

  val values = findValues.toVector
}