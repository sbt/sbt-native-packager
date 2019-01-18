package com.typesafe.sbt.packager.docker

/**
  * This represents a strategy to change the file permissions.
  */
sealed trait DockerPermissionStrategy
object DockerPermissionStrategy {

  /**
    * `None` does not attempt to change the file permissions.
    * This will inherit the host machine's group bits.
    */
  case object None extends DockerPermissionStrategy

  /**
    * `Run` calls `RUN` in the `Dockerfile`.
    * This could double the size of the resulting Docker image
    * because of the extra layer it creates.
    */
  case object Run extends DockerPermissionStrategy

  /**
    * `MultiStage` uses multi-stage Docker build to change
    * the file permissions.
    * https://docs.docker.com/develop/develop-images/multistage-build/
    */
  case object MultiStage extends DockerPermissionStrategy

  /**
    * `CopyChown` calls `COPY --chown` in the `Dockerfile`.
    * This option is provided for backward compatibility.
    * This will inherit the host machine's file mode.
    * Note that this option is not compatible with OpenShift which ignores
    * USER command and uses an arbitrary user to run the container.
    */
  case object CopyChown extends DockerPermissionStrategy
}

/**
  * This represents a type of file permission changes to run on the working directory.
  * Note that group file mode bits must be effective to be OpenShift compatible.
  */
sealed trait DockerChmodType {
  def argument: String
}
object DockerChmodType {

  /**
    * Gives read permission to users and groups.
    * Gives execute permission to users and groups, if +x flag is on for any.
    */
  case object UserGroupReadExecute extends DockerChmodType {
    def argument: String = "u=rX,g=rX"
  }

  /**
    * Gives read and write permissions to users and groups.
    * Gives execute permission to users and groups, if +x flag is on for any.
    */
  case object UserGroupWriteExecute extends DockerChmodType {
    def argument: String = "u=rwX,g=rwX"
  }

  /**
    * Copies user file mode bits to group file mode bits.
    */
  case object SyncGroupToUser extends DockerChmodType {
    def argument: String = "g=u"
  }

  /**
    * Use custom argument.
    */
  case class Custom(argument: String) extends DockerChmodType
}
