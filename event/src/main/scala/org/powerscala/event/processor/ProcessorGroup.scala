package org.powerscala.event.processor

import org.powerscala.event.ListenMode
import org.powerscala.Priority

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ProcessorGroup[Event, Response, Result](processors: List[EventProcessor[Event, Response, Result]]) {
  def and[NE >: Event, NV >: Response, NR >: Result](processor: EventProcessor[NE, NV, NR]): ProcessorGroup[NE, NV, NR] = {
    copy(processors = processor :: processors.asInstanceOf[List[EventProcessor[NE, NV, NR]]])
  }

  def on(f: Event => Response, priority: Priority = Priority.Normal) = listen(priority)(f)

  def listen(priority: Priority, modes: ListenMode*)(f: Event => Response) = {
    processors.map(ep => ep.listen(priority, modes: _*)(f))
  }

  def fire(event: Event, mode: ListenMode = ListenMode.Standard): List[Result] = {
    processors.map(ep => ep.fire(event, mode))
  }
}