package org.powerscala.json

import org.powerscala.Priority
import org.powerscala.event.processor.OptionProcessor

/**
 * @author Matt Hicks <matt@outr.com>
 */
class TypeEventConverter(processor: OptionProcessor[Any, Any]) {
  private var map = Map.empty[Class[_], Any => Any]

  processor.listen(Priority.Low) {
    case evt if evt != null => map.get(evt.getClass) match {
      case Some(f) => Some(f(evt))
      case None => None
    }
    case _ => None
  }

  def add(clazz: Class[_], f: Any => Any) = synchronized {
    map += clazz -> f
  }
}