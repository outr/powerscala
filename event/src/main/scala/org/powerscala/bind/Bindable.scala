package org.powerscala.bind

import org.powerscala.event.Listenable


//import org.powerscala.Listenable

/**
 * Bindable is an inheritable trait on mutable objects that allows binding to Listenable objects to
 * reflect the changes back to the Bindable.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait Bindable[T] extends Function1[T, Unit] with Listenable {
  /**
   * Binds this instance to get changes to <code>listenable</code> when they occur.
   *
   * @param listenable what to bind to
   */
  def bind(listenable: Listenable) = {
    val binding = new Binding[T](this, listenable.filters.target)
    listenable.listeners.synchronous += binding
    binding
  }

  /**
   * Binds this instance to get changes to <code>listenable</code> when they occur and are converted through the
   * <code>conversion</code> implicit function to represent the correct value.
   *
   * @param listenable what to bind to
   */
  def bindTo[S](listenable: Listenable)(implicit conversion: S => T) = {
    val binding = new Binding[S](conversion.andThen(this), listenable.filters.target)
    listenable.listeners.synchronous += binding
    binding
  }

  /**
   * Unbinds this instance from changes occurring on <code>listenable</code> when they occur.
   *
   * @param listenable to unbind from
   */
  def unbind(listenable: Listenable) = {
    listenable.listeners.values.find(l => l match {
      case binding: Binding[_] => binding.acceptFilter == listenable.filters.target
    }) match {
      case Some(listener) => listenable.listeners -= listener
      case None => // Not found
    }
  }
}