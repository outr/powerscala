package org.powerscala

/**
 * Version represents a version numbering.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class Version(major: Int = 1, minor: Int = 0, maintenance: Int = 0, build: Int = 0) extends Ordered[Version] {
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
    b.toString
  }

  override def toString = string

  def compare(that: Version) = if (major != that.major) {
    major.compare(that.major)
  } else if (minor != that.minor) {
    minor.compare(that.minor)
  } else if (maintenance != that.maintenance) {
    maintenance.compare(that.maintenance)
  } else if (build != that.build) {
    build.compare(that.build)
  } else {
    0
  }
}