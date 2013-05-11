package org.powerscala.event

/**
 * @author Matt Hicks <matt@outr.com>
 */
case class Change[T](oldValue: T, newValue: T)