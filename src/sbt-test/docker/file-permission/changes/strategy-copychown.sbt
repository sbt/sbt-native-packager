import com.typesafe.sbt.packager.docker._

dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerBaseImage := "fabric8/java-centos-openjdk8-jdk"

// opt-out of numeric USER
daemonUserUid in Docker := None
daemonUser in Docker := "daemon"
