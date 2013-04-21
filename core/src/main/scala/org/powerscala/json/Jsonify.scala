package org.powerscala.json

import util.parsing.json.JSON

import org.powerscala.reflect._

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
trait Jsonify {
  def parse(content: String) = JSON.parseFull(content) match {
    case Some(value) => parseJson(value.asInstanceOf[Map[String, Any]])
    case None => throw new NullPointerException("Unsupported JSON data: %s".format(content))
  }

  def parseJson(map: Map[String, Any]): Jsonify

  def generate(): String
}

object Jsonify {
  def getOrElse[T](name: String, default: T, map: Map[String, Any])(implicit manifest: Manifest[T]) = {
    if (map.contains(name)) {
      manifest.runtimeClass.convertTo[T](name, map(name))
    } else {
      default
    }
  }

  def stringify(s: String) = s match {
    case null => null
    case _ => "\"%s\"".format(s)
  }
}