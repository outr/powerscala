package org.powerscala.event.processor

import org.powerscala.event.{ListenMode, ListenerWrapper}

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class ProcessorGroup[E, V, R](processors: List[EventProcessor[E, V, R]]) {
  def and[NE >: E, NV >: V, NR >: R](processor: EventProcessor[NE, NV, NR]): ProcessorGroup[NE, NV, NR] = {
    copy(processors = processor :: processors.asInstanceOf[List[EventProcessor[NE, NV, NR]]])
  }

  def on(f: E => V): List[ListenerWrapper[E, V, R]] = listen()(f)

  def listen(modes: ListenMode*)(f: E => V): List[ListenerWrapper[E, V, R]] = {
    processors.map(ep => ep.listen(modes: _*)(f))
  }

  def fire(event: E, mode: ListenMode = ListenMode.Standard): List[R] = {
    processors.map(ep => ep.fire(event, mode))
  }
}