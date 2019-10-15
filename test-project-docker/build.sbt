enablePlugins(JavaAppPackaging, AshScriptPlugin)

name := "docker-test"
version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"
dockerBaseImage := "openjdk:8-jre-alpine"
dockerUpdateLatest := true
dockerGroupFiles := Seq(
  ("org*" -> 0),
  ("docker-test*" -> 1),
)
