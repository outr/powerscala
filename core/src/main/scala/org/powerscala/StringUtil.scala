package org.powerscala

object StringUtil {
  /**
   * Converts space and dash separated to camel-case
   */
  def toCamelCase(name: String) = "[- _]([a-zA-Z0-9])".r.replaceAllIn(name, m => m.group(1).toUpperCase)

  /**
   * Converts camelCase to dash-separated.
   */
  def fromCamelCase(name: String) = "([A-Z])".r.replaceAllIn(name, m => "-" + m.group(0).toLowerCase)

  /**
    * Generates a human readable label for this name.
    */
  def generateLabel(name: String) = {
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

  private val TrimRegex = """(\p{Z}*)(.*?)(\p{Z}*)""".r
  def trim(s: String) = s match {
    case TrimRegex(left, content, right) => content
  }

  def humanReadableByteCount(bytes: Long, si: Boolean = true): String = {
    val unit = if (si) 1000 else 1024
    if (bytes < unit) {
      s"$bytes B"
    } else {
      val exp = (math.log(bytes) / math.log(unit)).toInt
      val pre = (if (si) "kMGTPE" else "KMGTPE").charAt(exp - 1) + (if (si) "" else "i")
      "%.1f %sB".format(bytes / Math.pow(unit, exp), pre)
    }
  }
}