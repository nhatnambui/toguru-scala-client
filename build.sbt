import scala.util.Properties

name := "featurebee"
organization := "com.autoscout24"
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

version in ThisBuild := "1.0." + Properties.envOrElse("TRAVIS_BUILD_NUMBER", "0-SNAPSHOT")

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "io.spray" %%  "spray-json" % "1.3.2",
  "commons-io" % "commons-io" % "2.4",
  "com.typesafe.play" %% "play" % "2.3.9" % "optional",
  "com.typesafe.play" %% "play-test" % "2.3.9" % "optional",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.mockito" % "mockito-core" % "2.0.8-beta" % "test"
)

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 90

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
)

