name := "FAIG-SCALA"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.8"

// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test

libraryDependencies += "com.typesafe" % "config" % "1.3.2"

libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.1"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.11"