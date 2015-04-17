package org.powerscala.enum

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EnumEntry extends Product with Serializable {
  lazy val name = getClass.getSimpleName.substring(0, getClass.getSimpleName.length - 1)

  lazy val label = EnumEntry.generateLabel(name)

  lazy val parentClass = Class.forName(getClass.getName.substring(0, getClass.getName.indexOf('$') + 1))
  lazy val parentName = getClass.getName.substring(getClass.getName.lastIndexOf('.') + 1, getClass.getName.indexOf('$'))

  /**
   * Adds additional lookup validation to match on Enumerated.get
   *
   * @param s the String to validate
   * @return defaults to false
   */
  def isMatch(s: String) = false
}

object EnumEntry {
  private def generateLabel(name: String) = {
    val b = new StringBuilder
    var p = ' '
    name.foreach {
      case '$' => // Ignore $
      case c => {
        if (b.length > 1 && (p.isUpper || p.isDigit) && (!c.isUpper && !c.isDigit)) {
          b.insert(b.length - 1, ' ')
        }
        b.append(c)
        p = c
      }
    }
    b.toString().capitalize
  }
}