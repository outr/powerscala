import Dependencies._
import sbt.Keys._
import sbt._
import sbtunidoc.Plugin._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object PowerScalaBuild extends Build {
  lazy val root = project.in(file("."))
    .settings(sharedSettings())
    .settings(unidocSettings: _*)
    .settings(publishArtifact := false)
    .aggregate(core.js, core.jvm, command, concurrent, console, io)
  lazy val core = crossProject.crossType(CrossType.Pure).in(file("core"))
    .settings(withCompatUnmanagedSources(jsJvmCrossProject = true, include_210Dir = false, includeTestSrcs = true): _*)
    .settings(sharedSettings(Some("core")): _*)
    .settings(libraryDependencies += enumeratum)
  lazy val coreJs = core.js
  lazy val coreJvm = core.jvm
  lazy val command = project.in(file("command"))
    .settings(sharedSettings(Some("command")): _*)
  lazy val concurrent = project.in(file("concurrent"))
    .settings(sharedSettings(Some("concurrent")): _*)
    .settings(libraryDependencies += enumeratum)
  lazy val console = project.in(file("console"))
    .settings(sharedSettings(Some("console")): _*)
    .settings(libraryDependencies += jLine)
  lazy val io = project.in(file("io"))
    .settings(sharedSettings(Some("io")): _*)

  private def sharedSettings(projectName: Option[String] = None) = Seq(
    name := s"${Details.name}${projectName.map(pn => s"-$pn").getOrElse("")}",
    version := Details.version,
    organization := Details.organization,
    scalaVersion := Details.scalaVersion,
    crossScalaVersions := Details.scalaVersions,
    sbtVersion := Details.sbtVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases")
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies += scalaTest,
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

  /**
    * Helper function to add unmanaged source compat directories for different scala versions
    */
  private def withCompatUnmanagedSources(jsJvmCrossProject: Boolean, include_210Dir: Boolean, includeTestSrcs: Boolean): Seq[Setting[_]] = {
    def compatDirs(projectbase: File, scalaVersion: String, isMain: Boolean) = {
      val base = if (jsJvmCrossProject ) projectbase / ".." else projectbase
      CrossVersion.partialVersion(scalaVersion) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq(base / "compat" / "src" / (if (isMain) "main" else "test") / "scala-2.11").map(_.getCanonicalFile)
        case Some((2, scalaMajor)) if scalaMajor == 10 && include_210Dir => Seq(base / "compat" / "src" / (if (isMain) "main" else "test") / "scala-2.10").map(_.getCanonicalFile)
        case _ => Nil
      }
    }
    val unmanagedMainDirsSetting = Seq(
      unmanagedSourceDirectories in Compile ++= {
        compatDirs(projectbase = baseDirectory.value, scalaVersion = scalaVersion.value, isMain = true)
      }
    )
    if (includeTestSrcs) {
      unmanagedMainDirsSetting ++ {
        unmanagedSourceDirectories in Test ++= {
          compatDirs(projectbase = baseDirectory.value, scalaVersion = scalaVersion.value, isMain = false)
        }
      }
    } else {
      unmanagedMainDirsSetting
    }
  }
}

object Details {
  val organization = "org.powerscala"
  val name = "powerscala"
  val version = "2.0.5"
  val url = "http://outr.com"
  val licenseType = "Apache 2.0"
  val licenseURL = "http://opensource.org/licenses/Apache-2.0"
  val projectURL = "https://github.com/outr/powerscala"
  val repoURL = "https://github.com/outr/powerscala.git"
  val developerId = "darkfrog"
  val developerName = "Matt Hicks"
  val developerURL = "http://matthicks.com"

  val sbtVersion = "0.13.13"
  val scalaVersion = "2.12.1"
  val scalaVersions = List("2.12.1", "2.11.8")
}

object Dependencies {
  val enumeratum = "com.beachape" %% "enumeratum" % "1.5.7"
  val jLine = "org.jline" % "jline" % "3.1.3"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
}
