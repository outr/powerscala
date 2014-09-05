package org.powerscala.enum

import org.powerscala.StringUtil
import org.powerscala.reflect._
import scala.util.Random

/**
 * Enumerated represents the companion object for EnumEntry instances.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait Enumerated[E <: EnumEntry] extends FromString[E] {
  private val fields = getClass.fields.filter(f => f.returnType.hasType(classOf[EnumEntry])).zipWithIndex.map(t => new EnumField[E](t._1, t._2))
  private val namesMapLowerCase = fields.map(ef => List(ef.name.toLowerCase -> ef, StringUtil.generateLabel(ef.name).toLowerCase -> ef)).flatten.toMap
  private val namesMap = fields.map(ef => List(ef.name -> ef, StringUtil.generateLabel(ef.name) -> ef)).flatten.toMap

  /**
   * The name of this Enumerated.
   */
  lazy val name = getClass.getSimpleName.replaceAll("\\$", "")

  private[enum] def define(e: E) = fields.find(f => !f.isDefined) match {
    case Some(f) => f.define(e)
    case None => null.asInstanceOf[String] -> -1        // Not a defined field in the Enumerated instance
  }

  def values = fields.map(ef => ef.entry)         // TODO: optimize to pre-defined list after initialization
  lazy val length = fields.length

  /**
   * Retrieve the EnumEntry by name with a case-insensitive lookup.
   *
   * @param name to find the enum by.
   * @return EnumEntry or null if not found
   */
  def apply(name: String): E = apply(name, caseSensitive = false)

  def get(name: String) = get(name, caseSensitive = false)
  def get(name: String, caseSensitive: Boolean) = if (name != null && name.nonEmpty) {
    val exactMatch = if (caseSensitive) {
      namesMap.get(name).map(ef => ef.entry)
    } else {
      namesMapLowerCase.get(name.toLowerCase).map(ef => ef.entry)
    }
    if (exactMatch.isEmpty) {     // Do isMatch checks on entries
      values.find(e => e != null && e.isMatch(name))
    } else {
      exactMatch
    }
  } else {
    None
  }

  /**
   * Retrieve the EnumEntry by name.
   *
   * @param name the name of the EnumEntry as defined by the field.
   * @param caseSensitive defines whether the lookup should be case-sensitive. Defaults to false.
   * @return EnumEntry or null if not found
   */
  def apply(name: String, caseSensitive: Boolean) = {
    get(name, caseSensitive).getOrElse(throw new NullPointerException(s"Unable to find ${this.name} by name: $name."))
  }

  /**
   * Retrieve the EnumEntry by index.
   *
   * @param index of the EnumEntry.
   * @return EnumEntry or IndexOutOfBoundsException
   */
  def apply(index: Int) = values(index)

  def unapply(s: String) = get(s)

  /**
   * Retrieves a random enum.
   */
  def random = apply(Enumerated.r.nextInt(length))
}

case class EnumField[E <: EnumEntry](field: EnhancedField, index: Int) {
  val name = field.name
  @volatile private var _entry: E = _
  def entry = _entry

  def define(e: E) = {
    _entry = e
    name -> index
  }
  def isDefined = _entry != null
}

object Enumerated {
  private lazy val r = new Random()
}