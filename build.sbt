name := "conllpreprocessor"

version := "1.0-SNAPSHOT"

organization := "edu.arizona.cs"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "org.clulab" %% "processors-main" % "6.0.1",
  "org.clulab" %% "processors-corenlp" % "6.0.1",
  "org.clulab" %% "processors-models" % "6.0.1"
)
