import com.typesafe.sbt.packager.docker._

dockerPermissionStrategy := DockerPermissionStrategy.MultiStage
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerBaseImage := "fabric8/java-centos-openjdk8-jdk"
