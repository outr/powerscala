package org.powerscala.hierarchy.event

import org.powerscala.event.processor.EventProcessor
import org.powerscala.event.{ListenMode, Listenable, EventState}
import org.powerscala.hierarchy.ChildLike
import scala.annotation.tailrec

/**
 * AncestorProcessor processes up the ancestry tree through parents firing events in DescentOf mode.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait AncestorProcessor[E, V, R] extends EventProcessor[E, V, R] {
  /**
   * If true the Descendants processing will run upon receipt of event.
   */
  protected def processAncestors = true

  override protected def fireAdditional(state: EventState[E], mode: ListenMode, listenable: Listenable) = {
    super.fireAdditional(state, mode, listenable)

    if (mode == ListenMode.Standard && processAncestors) {      // Only process ancestors on standard processing
      // Process up to the ancestry tree
      fireUp(state, ChildLike.parentOf(listenable))
    }
  }

  @tailrec
  private def fireUp(state: EventState[E], element: Any): Unit = element match {
    case null => // Ran out
    case listenable: Listenable => {
      fireInternal(state, Descendants, listenable)
      fireUp(state, ChildLike.parentOf(element))
    }
    case _ => fireUp(state, ChildLike.parentOf(element))
  }
}

object AncestorProcessor {
  private val doNotProcessKey = "ancestorsDoNotProcess"

  /**
   * For the current event processing don't process ancestors.
   */
  def doNotProcess() = EventState.current.store(doNotProcessKey) = true

  def shouldProcess = !EventState.current.store.getOrElse(doNotProcessKey, false)
}