package org.powerscala

import org.powerscala.enum.{Enumerated, EnumEntry}

/**
 * ProcessingMode defines how an action will be processed.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed class ProcessingMode extends EnumEntry

object ProcessingMode extends Enumerated[ProcessingMode] {
  case object Synchronous extends ProcessingMode
  case object Asynchronous extends ProcessingMode
  case object Concurrent extends ProcessingMode

  val values = findValues.toVector
}