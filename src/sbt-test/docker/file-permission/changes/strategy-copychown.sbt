import com.typesafe.sbt.packager.docker._

dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerBaseImage := "fabric8/java-centos-openjdk8-jdk"

// opt-out of numeric USER
(Docker / daemonUserUid) := None
(Docker / daemonUser) := "daemon"
