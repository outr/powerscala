package org.powerscala.property

import org.powerscala.property.backing.{VariableBacking, Backing}
import org.powerscala.property.event.processor.{PropertyChangeProcessor, PropertyChangingProcessor, PropertyReadProcessor}
import org.powerscala.property.event.{PropertyChangeEvent, PropertyRead}
import org.powerscala.event.Listenable
import org.powerscala.hierarchy.ChildLike
import org.powerscala.bind.Bindable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Property[T](backing: Backing[T] = new VariableBacking[T], val default: Option[T] = None)
                 (implicit val parent: Listenable, val manifest: Manifest[T])
      extends PropertyLike[T]
      with Listenable
      with Bindable[T]
      with ChildLike[Listenable] {
  val read = new PropertyReadProcessor()(this)
  val changing = new PropertyChangingProcessor[T]()(this, manifest)
  val change = new PropertyChangeProcessor[T]()(this, Manifest.classType[PropertyChangeEvent[T]](classOf[PropertyChangeEvent[T]]))

  default match {
    case Some(value) => this := value
    case None => // No default
  }

  protected def hierarchicalParent = parent

  def apply() = {
    propertyRead()
    backing.getValue
  }

  def apply(value: T) = {
    propertyChanging(value) match {
      case Some(newValue) => propertyChange(newValue)
      case None => // Don't change the value
    }
  }

  protected def propertyRead() = {
    read.fire(PropertyRead)
  }

  protected def propertyChanging(value: T): Option[T] = changing.fire(value)

  protected def propertyChange(value: T) = {
    val oldValue = backing.getValue
    backing.setValue(value)
    change.fire(PropertyChangeEvent(this, oldValue, value))
  }
}

object Property {
  def apply[T](backing: Backing[T] = new VariableBacking[T], default: Option[T] = None)
              (implicit parent: Listenable = null, manifest: Manifest[T]) = new Property[T](backing, default)

  def fireChanged[T](property: Property[T]) = property.propertyChange(property.value)
}