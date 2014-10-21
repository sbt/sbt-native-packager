import NativePackagerKeys._

packagerSettings

name := "simple-test"

version := "0.1.0"

libraryDependencies ++= Seq(
  "com.typesafe" %% "config" % "1.2.1",
  "com.google.guava" % "guava" % "18.0"
)
