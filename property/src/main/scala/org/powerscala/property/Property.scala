package org.powerscala.property

import org.powerscala.bind.Bindable
import org.powerscala.enum.{EnumEntry, Enumerated}
import org.powerscala.event.Listenable
import org.powerscala.hierarchy.ChildLike
import org.powerscala.property.backing.{Backing, VariableBacking}
import org.powerscala.property.event.processor.{PropertyChangeProcessor, PropertyChangingProcessor, PropertyReadProcessor}
import org.powerscala.property.event.{PropertyChangeEvent, PropertyRead}
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Property[T](backing: Backing[T] = new VariableBacking[T], default: => Option[T] = None)
                 (implicit val parent: Listenable = null, val manifest: Manifest[T])
      extends ReadProperty[T]
      with WriteProperty[T]
      with Listenable
      with Bindable[T]
      with ChildLike[Listenable] {
  val read = new PropertyReadProcessor()(this)
  val changing = new PropertyChangingProcessor[T]()(this, manifest)
  val change = new PropertyChangeProcessor[T]()(this, Manifest.classType[PropertyChangeEvent[T]](classOf[PropertyChangeEvent[T]]))

  lazy val readOnlyView = new ReadOnlyPropertyLense(this)

  default match {
    case Some(value) => this := value
    case None => this := manifest.runtimeClass.defaultForType[T] // No default - use type default
  }

  protected def hierarchicalParent = parent

  def apply() = {
    propertyRead()
    backing.getValue
  }

  def apply(value: T): Unit = apply(value, EventHandling.Normal)

  def apply(value: T, handling: EventHandling) = propertyChanging(value) match {
    case Some(newValue) => handling match {
      case EventHandling.Normal => if (isChange(newValue)) {
        propertyChange(newValue)
      }
      case EventHandling.SuppressEvent => backing.setValue(newValue)
      case EventHandling.FireEvent => propertyChange(newValue)
    }
    case None => // The change was suppressed
  }

  def get = Option(apply())

  def and(other: Property[T]) = new PropertyGroup(List(other, this))

  protected def isChange(newValue: T) = newValue != value

  protected def propertyRead() = {
    read.fire(PropertyRead)
  }

  protected def propertyChanging(value: T): Option[T] = changing.fire(value)

  protected def propertyChange(value: T) = {
    val oldValue = backing.getValue
    backing.setValue(value)
    change.fire(PropertyChangeEvent(this, oldValue, value))
  }

  override def toString() = s"${getClass.getSimpleName}(${apply()})"
}

object Property {
  def apply[T](backing: Backing[T] = new VariableBacking[T], default: Option[T] = None)
              (implicit parent: Listenable = null, manifest: Manifest[T]) = new Property[T](backing, default)

  def fireChanged[T](property: Property[T]) = property.propertyChange(property.value)
}

class EventHandling extends EnumEntry

object EventHandling extends Enumerated[EventHandling] {
  /**
   * Fires an event only if the new value is different from the old value.
   */
  val Normal = new EventHandling
  /**
   * Never fires an event.
   */
  val SuppressEvent = new EventHandling
  /**
   * Fires an event no matter what the new value is.
   */
  val FireEvent = new EventHandling
}