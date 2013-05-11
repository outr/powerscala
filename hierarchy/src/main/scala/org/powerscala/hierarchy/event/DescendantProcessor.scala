package org.powerscala.hierarchy.event

import org.powerscala.event.processor.EventProcessor
import org.powerscala.event.{Listenable, ListenMode, EventState}
import org.powerscala.hierarchy.ParentLike
import org.powerscala.TypeFilteredIterator

/**
 * DescendantProcessor processes down the descendant tree through children firing events in Ancestors mode.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait DescendantProcessor[E, V, R] extends EventProcessor[E, V, R] {
  /**
   * If true the Ancestors processing will run upon receipt of event.
   */
  protected def processDescendants = true

  override protected def fireAdditional(state: EventState[E], mode: ListenMode, listenable: Listenable) {
    super.fireAdditional(state, mode, listenable)

    if (mode == ListenMode.Standard && processDescendants) {        // Only process descendants on standard processing
      // Process down the descendant tree
      TypeFilteredIterator[Listenable](ParentLike.descendants(listenable)).foreach {
        case childListenable => if (!state.isStopPropagation && DescendantProcessor.shouldProcess) {
          fireInternal(state, Ancestors, childListenable)
        }
      }
    }
  }
}

object DescendantProcessor {
  private val doNotProcessKey = "descendantsDoNotProcess"

  /**
   * For the current event processing don't process descendants.
   */
  def doNotProcess() = EventState.current(doNotProcessKey) = true

  def shouldProcess = !EventState.current.getOrElse(doNotProcessKey, false)
}