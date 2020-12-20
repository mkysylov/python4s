name := "python4s"

version := "1.0"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "com.github.jnr" % "jnr-ffi" % "2.2.1",
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "org.scalatest" %% "scalatest-flatspec" % "3.2.2" % "test"
)

concurrentRestrictions in Global += Tags.limit(Tags.Test, 1)