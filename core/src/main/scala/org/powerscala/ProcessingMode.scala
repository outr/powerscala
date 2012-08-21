package org.powerscala

/**
 * ProcessingMode defines how an action will be processed.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
sealed class ProcessingMode extends EnumEntry[ProcessingMode]

object ProcessingMode extends Enumerated[ProcessingMode] {
  val Synchronous = new ProcessingMode
//  val Asynchronous = new ProcessingMode
  val Concurrent = new ProcessingMode
}