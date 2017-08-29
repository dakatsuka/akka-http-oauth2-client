import ReleaseTransformations._

organization := "com.github.dakatsuka"

name := "akka-http-oauth2-client"

scalaVersion := "2.12.3"

crossScalaVersions := Seq("2.11.11", "2.12.3")

lazy val akkaHttpVersion = "10.0.9"
lazy val circeVersion    = "0.8.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"         % akkaHttpVersion,
  "io.circe"          %% "circe-generic"     % circeVersion,
  "io.circe"          %% "circe-parser"      % circeVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalatest"     %% "scalatest"         % "3.0.3" % "test"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Xfuture",
  "-target:jvm-1.8"
)

enablePlugins(ScalafmtPlugin)

scalafmtOnCompile := true

scalafmtTestOnCompile := true

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-Xlint")

licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/dakatsuka/akka-http-oauth2-client"))

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ =>
  false
}

releaseCrossBuild := true

publishTo := Some(
  if (isSnapshot.value) Opts.resolver.sonatypeSnapshots
  else Opts.resolver.sonatypeStaging
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/dakatsuka/akka-http-oauth2-client"),
    "scm:git@github.com:dakatsuka/akka-http-oauth2-client.git"
  )
)

developers := List(
  Developer(
    id = "dakatsuka",
    name = "Dai Akatsuka",
    email = "d.akatsuka@gmail.com",
    url = url("https://github.com/dakatsuka")
  )
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("+publishSigned", _)),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("+sonatypeRelease", _)),
  pushChanges
)
