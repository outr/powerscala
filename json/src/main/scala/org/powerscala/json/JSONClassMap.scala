package org.powerscala.json

import org.json4s.JValue
import org.powerscala.Priority
import org.powerscala.event.{ListenMode, Listener}
import org.powerscala.json.convert._
import org.powerscala.log.Logging

/**
 * @author Matt Hicks <matt@outr.com>
 */
object JSONClassMap extends Logging {
  private var map = Map.empty[Class[_], JSONConverter[Any, JValue]]

  val parserListener = new Listener[Any, Option[JValue]] {
    override val name = "parsers"
    override val priority = Priority.Low
    override val eventClass = classOf[Any]
    override val modes = List(ListenMode.Standard)

    override def receive(event: Any) = {
      debug(s"Parser Receive: $event (${event.getClass.getName})")
      map.get(event.getClass).map(f => f.toJSON(event))
    }
  }
  val readerListener = new Listener[JValue, Option[Any]] {
    override val name = "readers"
    override val priority = Priority.Low
    override val eventClass = classOf[JValue]
    override val modes = List(ListenMode.Standard)

    override def receive(event: JValue) = {
      debug(s"Reader Receive: $event (${event.getClass.getName})")
      map.get(event.getClass).map(f => f.fromJSON(event))
    }
  }

  // Default types
  register(BooleanSupport, classOf[java.lang.Boolean])
  register(IntSupport, classOf[java.lang.Integer])
  register(DoubleSupport, classOf[java.lang.Double])
  register(DecimalSupport)
  register(StringSupport)
  register(ListSupport, classOf[::[_]])
  register(MapSupport, classOf[Map.Map1[_, _]], classOf[Map.Map2[_, _]], classOf[Map.Map3[_, _]], classOf[Map.Map4[_, _]])

  def register[T, J <: JValue](converter: JSONConverter[T, J], aliasClasses: Class[_]*)
                              (implicit typeManifest: Manifest[T], jsonManifest: Manifest[J]) = synchronized {
    map += typeManifest.runtimeClass -> converter.asInstanceOf[JSONConverter[Any, JValue]]
    map += jsonManifest.runtimeClass -> converter.asInstanceOf[JSONConverter[Any, JValue]]
    aliasClasses.foreach(map += _ -> converter.asInstanceOf[JSONConverter[Any, JValue]])
  }
}