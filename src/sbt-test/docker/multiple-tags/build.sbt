enablePlugins(JavaAppPackaging)

name := "docker-test"

version := "0.1.0"

maintainer := "Gary Coady <gary@lyranthe.org>"

dockerAdditionalTags := Seq("0.1", "0")

dockerUpdateLatest := true
