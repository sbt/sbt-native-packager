enablePlugins(AkkaAppPackaging)

name := """test-akka"""

mainClass in Compile := Some("HelloKernel")

version := "1.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-kernel" % "2.3.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4"
)
