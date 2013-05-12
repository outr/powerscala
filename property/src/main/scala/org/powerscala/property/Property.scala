package org.powerscala.property

import org.powerscala.property.backing.{VariableBacking, Backing}
import org.powerscala.property.event.processor.{PropertyChangeProcessor, PropertyChangingProcessor, PropertyReadProcessor}
import org.powerscala.property.event.{PropertyChangeEvent, PropertyRead}
import org.powerscala.event.Listenable
import org.powerscala.hierarchy.ChildLike
import org.powerscala.bind.Bindable
import org.powerscala.reflect._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class Property[T](backing: Backing[T] = new VariableBacking[T])
                 (implicit val parent: Listenable, manifest: Manifest[T])
      extends ((T) => Unit)
      with (() => T)
      with Listenable
      with Bindable[T]
      with ChildLike[Listenable] {
  // Initialize for default value
  backing.setValue(manifest.runtimeClass.defaultForType[T])

  val read = new PropertyReadProcessor()(this)
  val changing = new PropertyChangingProcessor[T]()(this, manifest)
  val change = new PropertyChangeProcessor[T]()(this, Manifest.classType[PropertyChangeEvent[T]](classOf[PropertyChangeEvent[T]]))

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

  def :=(value: T) = apply(value)

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
  def apply[T](backing: Backing[T] = new VariableBacking[T])
              (implicit parent: Listenable = null, manifest: Manifest[T]) = new Property[T](backing)
}