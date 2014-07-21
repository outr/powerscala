import sbt._
import Keys._
import sbtunidoc.Plugin._

import Dependencies._

object PowerScalaBuild extends Build {
  val baseSettings = Defaults.defaultSettings ++ Seq(
    version := "1.6.5-SNAPSHOT",
    organization := "org.powerscala",
    scalaVersion := "2.11.1",
    libraryDependencies ++= Seq(
      scalaTest
    ),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"),
    publishTo <<= version {
      (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    fork := true,
    publishArtifact in Test := false,
    pomExtra := <url>http://powerscala.org</url>
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
      </developers>
  )

  private def createSettings(_name: String) = baseSettings ++ Seq(name := _name)

  lazy val root = Project("root", file("."), settings = unidocSettings ++ createSettings("powerscala-root"))
    .settings(publishArtifact in Compile := false, publishArtifact in Test := false)
    .aggregate(reflect, core, concurrent, communication, search, event, hierarchy, log, json, property, interpreter)
  lazy val reflect = Project("reflect", file("reflect"), settings = createSettings("powerscala-reflect"))
    .settings(libraryDependencies ++= Seq(asm, argonaut, reflections, "org.scala-lang" % "scala-reflect" % scalaVersion.value))
  lazy val core = Project("core", file("core"), settings = createSettings("powerscala-core"))
    .settings(libraryDependencies ++= Seq(akkaActors))
    .dependsOn(reflect)
  lazy val communication = Project("communication", file("communication"), settings = createSettings("powerscala-communication"))
    .settings(libraryDependencies ++= Seq(json4s))
    .dependsOn(core)
  lazy val concurrent = Project("concurrent", file("concurrent"), settings = createSettings("powerscala-concurrent"))
    .dependsOn(core)
  lazy val event = Project("event", file("event"), settings = createSettings("powerscala-event"))
    .dependsOn(core, concurrent)
  lazy val hierarchy = Project("hierarchy", file("hierarchy"), settings = createSettings("powerscala-hierarchy"))
    .dependsOn(core, event)
  lazy val log = Project("log", file("log"), settings = createSettings("powerscala-log"))
    .dependsOn(core, event)
  lazy val json = Project("json", file("json"), settings = createSettings("powerscala-json"))
    .settings(libraryDependencies ++= Seq(json4s))
    .dependsOn(event, log)
  lazy val search = Project("search", file("search"), settings = createSettings("powerscala-search"))
    .dependsOn(core, event)
    .settings(libraryDependencies ++= Seq(luceneCore, luceneAnalyzersCommon, luceneQueries, luceneQueryParser, luceneFacet))
  lazy val property = Project("property", file("property"), settings = createSettings("powerscala-property"))
    .dependsOn(core, event, hierarchy, log)
  lazy val interpreter = Project("interpreter", file("interpreter"), settings = createSettings("powerscala-interpreter"))
    .settings(libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % ))
    .dependsOn(reflect)
}

object Dependencies {
  val luceneVersion = "4.6.1"

  val akkaActors = "com.typesafe.akka" %% "akka-actor" % "latest.release"
  val argonaut = "io.argonaut" %% "argonaut" % "6.0.4"
  val asm = "org.ow2.asm" % "asm-all" % "latest.release"
  val json4s = "org.json4s" %% "json4s-jackson" % "latest.release"
  val luceneCore = "org.apache.lucene" % "lucene-core" % luceneVersion
  val luceneAnalyzersCommon = "org.apache.lucene" % "lucene-analyzers-common" % luceneVersion
  val luceneQueries = "org.apache.lucene" % "lucene-queries" % luceneVersion
  val luceneQueryParser = "org.apache.lucene" % "lucene-queryparser" % luceneVersion
  val luceneFacet = "org.apache.lucene" % "lucene-facet" % luceneVersion
  val h2 = "com.h2database" % "h2" % "latest.release"
  val reflections = "org.reflections" % "reflections" % "0.9.8"
  val scalaTest = "org.scalatest" %% "scalatest" % "latest.release" % "test"
}
