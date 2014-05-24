package org.powerscala.enum

import org.powerscala.reflect._
import scala.util.Random

/**
 * Enumerated represents the companion object for EnumEntry instances.
 *
 * @author Matt Hicks <matt@outr.com>
 */
trait Enumerated[E <: EnumEntry] {
  implicit val thisEnumerated = this.asInstanceOf[Enumerated[EnumEntry]]

  private var initialized = false
  private var enums = List.empty[E]
  private lazy val enumFields = getClass.fields.collect {
    case f if !f.isStatic && f.hasType(getClass.nonCompanion.javaClass) => f
  }
  private lazy val nameMap = enumFields.map(field2Entry).toMap
  private lazy val valueMap = nameMap.map(t => t._2 -> t._1)

  private def field2Entry(f: EnhancedField) = {
    val name = f.name
    val value = f[E](this)
    if (value == null) throw new RuntimeException(s"Enumerated requesting names before all EnumEntries have initialized (field: ${f.name}!")
    name -> value
  }

  /**
   * The name of this Enumerated.
   */
  lazy val name = getClass.getSimpleName.replaceAll("\\$", "")
  lazy val values = {
    initialized = true
    enums.reverse
  }
  lazy val length = values.length

  /**
   * Retrieve the EnumEntry by name with a case-insensitive lookup.
   *
   * @param name to find the enum by.
   * @return EnumEntry or null if not found
   */
  def apply(name: String): E = apply(name, caseSensitive = false)

  def get(name: String): Option[E] = Option(apply(name))

  /**
   * Retrieve the EnumEntry by name.
   *
   * @param name the name of the EnumEntry as defined by the field.
   * @param caseSensitive defines whether the lookup should be case-sensitive. Defaults to false.
   * @return EnumEntry or null if not found
   */
  def apply(name: String, caseSensitive: Boolean) = if (caseSensitive) {
    nameMap(name)
  } else {
    values.find(e => (e.name != null && e.name.equalsIgnoreCase(name)) || e.isMatch(name)).getOrElse(null.asInstanceOf[E])
  }

  /**
   * Retrieve the EnumEntry by index.
   *
   * @param index of the EnumEntry.
   * @return EnumEntry or IndexOutOfBoundsException
   */
  def apply(index: Int) = values(index)

  def unapply(s: String) = get(name)

  /**
   * Retrieves a random enum.
   */
  def random = apply(Enumerated.r.nextInt(length))

  protected[enum] def +=(enum: E) = synchronized {
    enums = enum :: enums
  }

  protected[enum] def enumName(enum: EnumEntry) = valueMap.getOrElse(enum.asInstanceOf[E], null)
  protected[enum] def enumOrdinal(enum: EnumEntry) = values.indexOf(enum)
}

object Enumerated {
  private lazy val r = new Random()
}