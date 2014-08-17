import _root_.sbt.Keys._

name := "taobao-fetcher"

version := "1.0"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

//scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.0",
  "org.seleniumhq.selenium" % "selenium-java" % "2.42.0",
  "play" %% "play" % "2.1.0"
)
