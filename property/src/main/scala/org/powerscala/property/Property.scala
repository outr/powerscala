package org.powerscala.property

import org.powerscala.property.backing.{VariableBacking, Backing}
import org.powerscala.property.event.processor.{PropertyChangeProcessor, PropertyChangingProcessor, PropertyReadProcessor}
import org.powerscala.property.event.PropertyRead
import org.powerscala.event.Listenable
import org.powerscala.hierarchy.ChildLike
import org.powerscala.bind.Bindable

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Property[T](backing: Backing[T] = new VariableBacking[T])
                 (implicit val parent: Listenable = null, manifest: Manifest[T])
      extends ((T) => Unit)
      with (() => T)
      with Listenable
      with Bindable[T]
      with ChildLike[Listenable] {
  val read = new PropertyReadProcessor
  val changing = new PropertyChangingProcessor
  val change = new PropertyChangeProcessor[T]

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

  protected def propertyChanging(value: T): Option[T] = Some(value)    // TODO: fire event

  protected def propertyChange(value: T) = {
    backing.setValue(value)
    // TODO: fire event
  }
}
