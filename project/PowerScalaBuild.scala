import sbt._
import Keys._

import Dependencies._

object PowerScalaBuild extends Build {
  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "1.0",
    organization := "org.powerscala",
    scalaVersion := "2.9.2",
    libraryDependencies ++= Seq(
      scalaTest
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers ++= Seq("Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomExtra := (
      <url>http://powerscala.org</url>
        <licenses>
          <license>
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <developerConnection>scm:https://github.com/darkfrog26/powerscala.git</developerConnection>
          <connection>scm:https://github.com/darkfrog26/powerscala.git</connection>
          <url>https://github.com/darkfrog26/powerscala</url>
        </scm>
        <developers>
          <developer>
            <id>darkfrog</id>
            <name>Matt Hicks</name>
            <url>http://matthicks.com</url>
          </developer>
        </developers>)
  )

  private def createSettings(_name: String) = baseSettings ++ Seq(name := _name)

  lazy val root = Project("root", file("."), settings = createSettings("powerscala-root"))
    .settings(publishArtifact in Compile := false, publishArtifact in Test := false)
    .aggregate(core, concurrent, convert, datastore, event, hierarchy, property, reflect)
  lazy val core = Project("core", file("core"), settings = createSettings("powerscala-core"))
    .dependsOn(reflect)
  lazy val concurrent = Project("concurrent", file("concurrent"), settings = createSettings("powerscala-concurrent"))
    .dependsOn(core)
  lazy val convert = Project("convert", file("convert"), settings = createSettings("powerscala-convert"))
    .dependsOn(core)
  lazy val datastore = Project("datastore", file("datastore"), settings = createSettings("powerscala-datastore"))
    .dependsOn(core, event, convert)
    .settings(libraryDependencies += mongodb)
  lazy val event = Project("event", file("event"), settings = createSettings("powerscala-event"))
    .dependsOn(core, concurrent)
  lazy val hierarchy = Project("hierarchy", file("hierarchy"), settings = createSettings("powerscala-hierarchy"))
    .dependsOn(core, event)
  lazy val property = Project("property", file("property"), settings = createSettings("powerscala-property"))
    .dependsOn(core, event, hierarchy)
  lazy val reflect = Project("reflect", file("reflect"), settings = createSettings("powerscala-reflect"))
    .settings(libraryDependencies ++= Seq(asm, paranamer))
}

object Dependencies {
  val asm = "org.ow2.asm" % "asm-all" % "4.0"
  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.5"
  val scalaTest = "org.scalatest" % "scalatest_2.9.1" % "1.7.1" % "test"
  val mongodb = "org.mongodb" % "mongo-java-driver" % "2.8.0"
}