import scala.util.Properties

name := "featurebee"
organization in ThisBuild := "com.autoscout24"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

version in ThisBuild := "1.0." + Properties.envOrElse("TRAVIS_BUILD_NUMBER", "0-SNAPSHOT")

scalaVersion in ThisBuild := "2.11.8"
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings",
  "-Yno-adapted-args", "-Xmax-classfile-name", "130")

val playVersion = "2.5.4"

libraryDependencies in ThisBuild ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "commons-io" % "commons-io" % "2.4",
  "org.scalactic" %% "scalactic" % "2.2.5",
  "com.typesafe.play" %% "play" % playVersion % "optional",
  "com.typesafe.play" %% "play-test" % playVersion % "optional",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.mockito" % "mockito-core" % "2.0.8-beta" % "test"
)

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 90

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

resolvers in ThisBuild ++= Seq(
  Classpaths.sbtPluginReleases,
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

lazy val root = project.in( file(".") )
    .aggregate(s3registryRef)

lazy val s3registry: Project = (project in file("s3-registry"))
  .dependsOn(root)
  .settings(
    name := "featurebee-s3-registry",
    libraryDependencies in ThisBuild ++= Seq(
      "com.amazonaws" % "aws-java-sdk" % "1.11.2"
      )
  )
lazy val s3registryRef = LocalProject("s3registry")


