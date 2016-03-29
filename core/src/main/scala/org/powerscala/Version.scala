package org.powerscala

/**
 * Version represents a version numbering.
 */
case class Version(major: Int = 1, minor: Int = 0, maintenance: Int = 0, build: Int = 0, extra: Option[String] = None, original: Option[String] = None) extends Ordered[Version] {
  private lazy val string = {
    val b = new StringBuilder
    b.append(general)
    if (build > 0) {
      b.append('.')
      b.append(build)
    }
    extra match {
      case Some(e) => b.append(s"-$e")
      case None => // No extra
    }
    b.toString()
  }

  lazy val general = s"$major.$minor.$maintenance"

  def snapshot: Boolean = extra.contains("SNAPSHOT")

  override def toString = original.getOrElse(string)

  def compare(that: Version) = if (major != that.major) {
    major.compare(that.major)
  } else if (minor != that.minor) {
    minor.compare(that.minor)
  } else if (maintenance != that.maintenance) {
    maintenance.compare(that.maintenance)
  } else if (build != that.build) {
    build.compare(that.build)
  } else if (extra != that.extra && extra != null) {
    extra.getOrElse("").compare(that.extra.getOrElse(""))
  } else if (extra != that.extra && that.extra != null) {
    -1
  } else {
    0
  }
}

object Version {
  val Zero = Version(0)
  val Matcher = """(\d+)[.]?(\d*)[.]?(\d*)[.]?(\d*)[-]?(.*)""".r

  def apply(version: String): Version = version match {
    case Version(v) => v
    case _ => throw new RuntimeException(s"Unable to parse version from: $version.")
  }

  def unapply(version: String): Option[Version] = version match {
    case null | "" => None
    case Matcher(major, minor, maintenance, build, extra) => {
      Some(Version(n(major), n(minor), n(maintenance), n(build), if (extra != null && extra.nonEmpty) Some(extra) else None, Some(version)))
    }
    case _ => None
  }

  private def n(s: String) = s match {
    case "" => 0
    case _ => s.toInt
  }
}
