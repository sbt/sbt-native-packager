package com.typesafe.sbt
package packager
package docker
import Keys._
import universal._
import sbt._

import sbt.Keys.cacheDirectory

trait DockerPlugin extends Plugin with UniversalPlugin with DockerKeys {
  val Docker = config("docker") extend Universal

  private[this] final def makeDockerContent(dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, execName: String) = {
    Dockerfile(
      Cmd("FROM", dockerBaseImage),
      Cmd("MAINTAINER", maintainer),
      Cmd("ADD", "files %s" format dockerBaseDirectory),
      Cmd("WORKDIR", dockerBaseDirectory),
      ExecCmd("RUN", "chown", "-R", daemonUser, "."),
      Cmd("USER", daemonUser),
      Cmd("ENTRYPOINT", "%s/bin/%s" format (dockerBaseDirectory, execName)),
      ExecCmd("CMD")
    ).makeContent
  }

  private[this] final def setDockerLayout(directoryName: String, mappings: Seq[(java.io.File, String)], dockerFile: java.io.File) = {
    def moveMapping(old: (java.io.File, String)) = {
      old._1 -> ("%s/%s" format (directoryName, old._2))
    }

    (mappings map moveMapping) :+ (dockerFile -> "Dockerfile")
  }

  private[this] final def generateDockerConfig(
    dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, normalizedName: String, target: File) = {
    val dockerContent = makeDockerContent(dockerBaseImage, dockerBaseDirectory, maintainer, daemonUser, normalizedName)

    val f = target / "Dockerfile"
    IO.write(f, dockerContent)
    f
  }

  def dockerSettings: Seq[Setting[_]] = inConfig(Docker)(Seq(
    daemonUser := "daemon",
    dockerBaseImage := "dockerfile/java",
    dockerBaseDirectory := "/opt/docker",
    stagingDirectory <<= (target, normalizedName) {
      case (target, normalizedName) =>
        (target / "docker" / normalizedName)
    },
    stage <<= (cacheDirectory, stagingDirectory, mappings) map stageFiles("docker"),
    dockerGenerateConfig <<=
      (dockerBaseImage, dockerBaseDirectory, maintainer in Docker, daemonUser in Docker, normalizedName in Docker, target in Docker) map {
        case (dockerBaseImage, dockerBaseDirectory, maintainer, daemonUser, normalizedName, target) =>
          generateDockerConfig(dockerBaseImage, dockerBaseDirectory, maintainer, daemonUser, normalizedName, target)
      },
    mappings <<= (mappings in Universal, dockerGenerateConfig) map {
      case (mappings, dockerfile) =>
        setDockerLayout("files", mappings, dockerfile)
    }
  ))

  private[this] def stageFiles(config: String)(cacheDirectory: File, to: File, mappings: Seq[(File, String)]): Unit = {
    val cache = cacheDirectory / ("packager-mappings-" + config)
    val copies = mappings map {
      case (file, path) => file -> (to / path)
    }
    Sync(cache)(copies)
    // Now set scripts to executable using Java's lack of understanding of permissions.
    // TODO - Config file user-readable permissions....
    for {
      (from, to) <- copies
      if from.canExecute
    } to.setExecutable(true)
  }
}
