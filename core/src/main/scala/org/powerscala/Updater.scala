package org.powerscala

import annotation.tailrec

/**
 * Updater contains a list of Updatables that are updated upon upon the update of this class.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Updater extends Updatable {
  private var initialized = false
  private var updatables: List[Updatable] = Nil

  /**
   * Allow initialization of content to occur before first update.
   */
  protected def initialize() = {
  }

  override def update(delta: Double) = {
    if (!initialized) {
      initialize()
      initialized = true
    }

    super.update(delta)

    doUpdate(delta, updatables)
  }

  @tailrec
  private def doUpdate(delta: Double, list: List[Updatable]): Unit = {
    if (!list.isEmpty) {
      val u = list.head
      u.update(delta)
      u match {
        case f: Finishable if (f.isFinished) => remove(u)
        case _ =>
      }
      doUpdate(delta, list.tail)
    }
  }

  protected[powerscala] def add(updatable: Updatable) = synchronized {
    updatables = updatable :: updatables
  }

  protected[powerscala] def remove(updatable: Updatable) = synchronized {
    updatables = updatables.filterNot(u => u == updatable)
  }
}