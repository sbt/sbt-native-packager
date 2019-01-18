import com.typesafe.sbt.packager.docker._

dockerPermissionStrategy := DockerPermissionStrategy.MultiStage
dockerChmodType          := DockerChmodType.UserGroupWriteExecute
