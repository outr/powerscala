package org.powerscala.event

import org.powerscala.ref.ReferenceType
import org.powerscala.hierarchy.{Child, Parent}
import org.powerscala.bus.Bus

/**
 * Listenable can be mixed in to provide the ability for event management on an object.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 * Date: 12/2/11
 */
trait Listenable {
  def bus = Bus()

  protected[event] var listenersList: List[Listener] = Nil

  protected[event] val localizedBus = new Bus()

//  protected[event] val asynchronousActor = new DaemonActor {
//    def act() {
//      loop {
//        react {
//          case f: Function0[_] => f()
//        }
//      }
//    }
//  }.start()

  protected[event] def addListener(listener: Listener, referenceType: ReferenceType = ReferenceType.Soft, localized: Boolean = false) = synchronized {
    listenersList = (listener :: listenersList.reverse).reverse
    if (localized) {
      localizedBus.add(listener, referenceType)
    } else {
      bus.add(listener, referenceType)
    }
    listener
  }

  protected[event] def removeListener(listener: Listener) = synchronized {
    listenersList = listenersList.filterNot(l => l == listener)
    localizedBus.remove(listener)
    bus.remove(listener)
    listener
  }

  def clearListeners() = synchronized {
    listenersList.foreach {
      case listener => removeListener(listener)
    }
    listenersList = Nil
  }

  lazy val listeners = EventListenerBuilder(this)

  /**
   * Adds change listeners to the Listenables to invoke the supplied function immediately when a
   * change occurs.
   *
   * Convenience method for Listenable.onChange.
   */
  def onChange(listenables: Listenable*)(f: => Any) = Listenable.onChange(listenables: _*)(f)

  /**
   * Allows single instantiation of a listener listening to multiple Listenables simultaneously.
   *
   * Convenience method for Listenable.multiple.
   */
  def listenTo(listenables: Listenable*) = Listenable.listenTo(listenables: _*)

  object filters {
    val target = TargetFilter(Listenable.this)

    def targets(listenables: Listenable*): Event => Boolean = {
      case event => listenables.contains(event.target)
    }

    def descendant(depth: Int = Int.MaxValue, includeCurrent: Boolean = false): Event => Boolean = {
      case event => event.target match {
        case _ if (depth == 0) => target(event)
        case child: Child if (Child.hasAncestor(child, Listenable.this, depth)) => true
        case t if (includeCurrent && t == Listenable.this) => true
        case _ => false
      }
    }

    def child() = descendant(1)

    def ancestor(depth: Int = Int.MaxValue, includeCurrent: Boolean = false): Event => Boolean = {
      case event => event.target match {
        case _ if (depth == 0) => target(event)
        case parent: Parent if (parent.hierarchy.hasDescendant(Listenable.this, depth)) => true
        case t if (includeCurrent && t == Listenable.this) => true
        case _ => false
      }
    }

    def parent() = ancestor(1)

    def thread(thread: Thread): Event => Boolean = {
      case event => event.thread == thread
    }
  }

  def fire(event: Event) = Event.fire(event, this)
}

object Listenable {
  /**
   * Adds change listeners to the Listenables to invoke the supplied function immediately when a
   * change occurs.
   */
  def onChange(listenables: Listenable*)(f: => Unit) = listenables.foreach(l => l.listeners.synchronous {
    case event: ChangeEvent => f
  })

  /**
   * Allows single instantiation of a listener listening to multiple Listenables simultaneously.
   */
  def listenTo(listenables: Listenable*) = listenables.head.listeners.filter.targets(listenables: _*)
}