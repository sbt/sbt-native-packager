import com.typesafe.sbt.packager.docker._

enablePlugins(JavaAppPackaging)
enablePlugins(DockerSpotifyClientPlugin)

name := "docker-test"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"
