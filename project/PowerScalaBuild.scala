import Dependencies._
import sbt.Keys._
import sbt._
import sbtunidoc.Plugin._

object PowerScalaBuild extends Build {
  lazy val root = Project("root", file("."), settings = unidocSettings)
    .settings(name := "PowerScala", publishArtifact in Compile := false, publishArtifact in Test := false, publish := {})
    .aggregate(core, concurrent, console)
  lazy val core = project("core").withDependencies(enumeratum)
  lazy val concurrent = project("concurrent").dependsOn(core)
  lazy val console = project("console").withDependencies(jLine).dependsOn(core)

  private def project(projectName: String) = Project(id = projectName, base = file(projectName)).settings(
    name := s"${Details.name}-$projectName",
    version := Details.version,
    organization := Details.organization,
    scalaVersion := Details.scalaVersion,
    sbtVersion := Details.sbtVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases")
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) {
          Some("snapshots" at nexus + "content/repositories/snapshots")
        } else {
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
        }
    },
    publishArtifact in Test := false,
    pomExtra := <url>${Details.url}</url>
      <licenses>
        <license>
          <name>{Details.licenseType}</name>
          <url>{Details.licenseURL}</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <developerConnection>scm:{Details.repoURL}</developerConnection>
        <connection>scm:{Details.repoURL}</connection>
        <url>{Details.projectURL}</url>
      </scm>
      <developers>
        <developer>
          <id>{Details.developerId}</id>
          <name>{Details.developerName}</name>
          <url>{Details.developerURL}</url>
        </developer>
      </developers>
  )

  implicit class EnhancedProject(project: Project) {
    def withDependencies(modules: ModuleID*): Project = project.settings(libraryDependencies ++= modules)
  }
}

object Details {
  val organization = "org.powerscala"
  val name = "powerscala"
  val version = "2.0.0-SNAPSHOT"
  val url = "http://outr.com"
  val licenseType = "Apache 2.0"
  val licenseURL = "http://opensource.org/licenses/Apache-2.0"
  val projectURL = "https://github.com/outr/powerscala"
  val repoURL = "https://github.com/outr/powerscala.git"
  val developerId = "darkfrog"
  val developerName = "Matt Hicks"
  val developerURL = "http://matthicks.com"

  val sbtVersion = "0.13.11"
  val scalaVersion = "2.11.7"
}

object Dependencies {
  val enumeratum = "com.beachape" %% "enumeratum" % "1.3.6"
  val jLine = "jline" % "jline" % "2.13"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test"
}
