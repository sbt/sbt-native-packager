import com.typesafe.sbt.packager.docker._

dockerPermissionStrategy := DockerPermissionStrategy.CopyChown
dockerBaseImage          := "fabric8/java-centos-openjdk8-jdk"
