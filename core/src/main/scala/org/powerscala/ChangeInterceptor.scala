package org.powerscala

/**
 * ChangeInterceptor is used to intercept and potentially modify a value when mixed in.
 *
 * @author Matt Hicks <mhicks@powerscala.org>
 */
trait ChangeInterceptor[T] {
  private var _interceptors: List[Interceptor[T]] = Nil

  object interceptors {
    /**
     * Adds an interceptor.
     */
    def +=(interceptor: Interceptor[T]) = synchronized {
      _interceptors = interceptor :: _interceptors
    }

    /**
     * Removes an interceptor.
     */
    def -=(interceptor: Interceptor[T]) = synchronized {
      _interceptors = _interceptors.filterNot(i => i eq interceptor)
    }
  }

  /**
   * Called when the value changes to invoke the interceptors and returns the applied value.
   */
  def change(oldValue: T, newValue: T): T = {
    _interceptors.foldLeft(newValue)((current, interceptor) => interceptor(oldValue, newValue, current).getOrElse(current))
  }
}