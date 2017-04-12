import com.typesafe.sbt.packager.docker._

enablePlugins(JavaAppPackaging)

name := "docker-commands"
version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

dockerUpdateLatest := true
dockerCommands := Seq(
  Cmd("FROM", "openjdk:latest"),
  Cmd("MAINTAINER", maintainer.value),
  ExecCmd("CMD", "echo", "Hello, World from Docker")
)
