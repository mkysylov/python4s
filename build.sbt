name := "python4s"

version := "1.0"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "com.github.jnr" % "jnr-ffi" % "2.1.10",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)