enablePlugins(JavaAppPackaging)

name := "docker-alias-test"

version := "0.1.0"

dockerAlias := DockerAlias(None, None, "docker-alias-test", Some("0.1.0"))
