name := "assignment2"

version := "0.1"

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.9"

libraryDependencies += "ch.qos.logback"% "logback-classic"%"1.1.3"%Runtime

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.0" % "test"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % AkkaVersion% Test





// set the main class for sbt run

mainClass in (Compile,run) := Some("NbodyMain")