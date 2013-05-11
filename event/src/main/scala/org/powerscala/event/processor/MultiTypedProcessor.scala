package org.powerscala.event.processor

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait MultiTypedProcessor[V, R] {
  private var map = Map.empty[Class[_], EventProcessor[_, V, R]]

  def apply[E](implicit manifest: Manifest[E]) = synchronized {
    map.get(manifest.runtimeClass) match {
      case Some(processor) => processor
      case None => {
        val processor = create(manifest.runtimeClass)
        map += manifest.runtimeClass -> processor
        processor
      }
    }
  }

  protected def create(clazz: Class[_]): EventProcessor[_, V, R]
}
