package org.powerscala

/**
 * Version represents a version numbering.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class Version(major: Int = 1, minor: Int = 0, maintenance: Int = 0, build: Int = 0, extra: String = null) extends Ordered[Version] {
  private lazy val string = {
    val b = new StringBuilder
    b.append(major)
    if (minor > 0 || maintenance > 0 || build > 0) {
      b.append('.')
      b.append(minor)
      if (maintenance > 0 || build > 0) {
        b.append('.')
        b.append(maintenance)
        if (build > 0) {
          b.append('.')
          b.append(build)
        }
      }
    }
    if (extra != null && extra.nonEmpty) {
      b.append('-')
      b.append(extra)
    }
    b.toString()
  }

  lazy val general = s"$major.$minor.$maintenance"

  override def toString = string

  def compare(that: Version) = if (major != that.major) {
    major.compare(that.major)
  } else if (minor != that.minor) {
    minor.compare(that.minor)
  } else if (maintenance != that.maintenance) {
    maintenance.compare(that.maintenance)
  } else if (build != that.build) {
    build.compare(that.build)
  } else if (extra != that.extra && extra != null) {
    extra.compare(that.extra)
  } else if (extra != that.extra && that.extra != null) {
    -1
  } else {
    0
  }
}

object Version {
  val Matcher = """(\d+)[.]?(\d*)[.]?(\d*)[.]?(\d*)[-]?(.*)""".r

  def apply(version: String): Version = version match {
    case Matcher(major, minor, maintenance, build, other) => {
      Version(n(major), n(minor), n(maintenance), n(build), other)
    }
  }

  private def n(s: String) = s match {
    case "" => 0
    case _ => s.toInt
  }
}
