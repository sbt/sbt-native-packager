enablePlugins(JavaAppPackaging)

name := "test-busybox-create-user"

version := "0.1.0"

maintainer := "Boris Capitanu <borice@hotmail.com>"
dockerBaseImage := "anapsix/alpine-java:8"
daemonUserUid in Docker := Some("2000")
daemonUser in Docker := "appuser"
daemonGroupGid in Docker := Some("3000")
daemonGroup in Docker := "appgroup"
