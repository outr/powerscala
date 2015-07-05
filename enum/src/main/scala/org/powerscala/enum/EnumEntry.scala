package org.powerscala.enum

/**
 * @author Matt Hicks <matt@outr.com>
 */
trait EnumEntry extends Product with Serializable {
  // getSimpleName doesn't work with nested classes, see
  // https://issues.scala-lang.org/browse/SI-5425
  private lazy val fullName = getClass.getName.init
  private lazy val dollar = fullName.lastIndexOf('$') + 1
  private lazy val dot = fullName.lastIndexOf('.') + 1
  private lazy val fullParentName = fullName.take(dollar)

  lazy val name  = fullName.drop(dollar)
  lazy val label = EnumEntry.generateLabel(name)

  lazy val parentName  = fullParentName.drop(dot).init.map(c => if (c == '$') '.' else c)
  lazy val parentClass = Class.forName(fullParentName)

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