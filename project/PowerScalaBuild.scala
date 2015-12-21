import Dependencies._
import sbt.Keys._
import sbt._
import sbtunidoc.Plugin._

object PowerScalaBuild extends Build {
  val baseSettings = Defaults.coreDefaultSettings ++ Seq(
    version := "1.6.11",
    organization := "org.powerscala",
    scalaVersion := "2.11.7",
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
    .aggregate(enum, reflect, core, concurrent, event, hierarchy, json, log, property, interpreter)
  lazy val enum = Project("enum", file("enum"), settings = createSettings("powerscala-enum"))
    .settings(libraryDependencies += enumeratum)
  lazy val reflect = Project("reflect", file("reflect"), settings = createSettings("powerscala-reflect"))
    .settings(libraryDependencies ++= Seq(asm, reflections, "org.scala-lang" % "scala-reflect" % scalaVersion.value))
  lazy val core = Project("core", file("core"), settings = createSettings("powerscala-core"))
    .settings(libraryDependencies ++= Seq(akkaActors, jLine))
    .dependsOn(enum, reflect)
  lazy val concurrent = Project("concurrent", file("concurrent"), settings = createSettings("powerscala-concurrent"))
    .dependsOn(core)
  lazy val event = Project("event", file("event"), settings = createSettings("powerscala-event"))
    .dependsOn(core, concurrent)
  lazy val hierarchy = Project("hierarchy", file("hierarchy"), settings = createSettings("powerscala-hierarchy"))
    .dependsOn(core, event)
  lazy val log = Project("log", file("log"), settings = createSettings("powerscala-log"))
    .dependsOn(core, event)
  lazy val json = Project("json", file("json"), settings = createSettings("powerscala-json"))
    .settings(libraryDependencies ++= Seq(json4sNative))
    .dependsOn(event, log)
  lazy val property = Project("property", file("property"), settings = createSettings("powerscala-property"))
    .dependsOn(core, event, hierarchy, log)
  lazy val interpreter = Project("interpreter", file("interpreter"), settings = createSettings("powerscala-interpreter"))
    .settings(libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % ))
    .dependsOn(reflect)
}

object Dependencies {
  val enumeratum = "com.beachape" %% "enumeratum" % "1.1.0"
  val akkaActors = "com.typesafe.akka" %% "akka-actor" % "2.3.13"
  val asm = "org.ow2.asm" % "asm-all" % "5.0.3"
  val jLine = "jline" % "jline" % "2.12.1"
  val json4sNative = "org.json4s" %% "json4s-native" % "3.2.11"
  val reflections = "org.reflections" % "reflections" % "0.9.9"
  val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"
}
