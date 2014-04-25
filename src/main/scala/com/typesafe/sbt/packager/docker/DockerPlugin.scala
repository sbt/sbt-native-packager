package com.typesafe.sbt
package packager
package docker
import Keys._
import universal._
import sbt._

import sbt.Keys.cacheDirectory
import universal.Keys.stage

trait DockerPlugin extends Plugin with UniversalPlugin with DockerKeys {
  val Docker = config("docker") extend Universal

  private[this] final def makeDockerContent(dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, name: String) = {
    Dockerfile(
      Cmd("FROM", dockerBaseImage),
      Cmd("MAINTAINER", maintainer),
      Cmd("ADD", "files /"),
      Cmd("WORKDIR", "%s/bin" format dockerBaseDirectory),
      ExecCmd("RUN", "chown", "-R", daemonUser, ".."),
      Cmd("USER", daemonUser),
      ExecCmd("ENTRYPOINT", name),
      ExecCmd("CMD")
    ).makeContent
  }

  private[this] final def generateDockerConfig(
    dockerBaseImage: String, dockerBaseDirectory: String, maintainer: String, daemonUser: String, normalizedName: String, target: File) = {
    val dockerContent = makeDockerContent(dockerBaseImage, dockerBaseDirectory, maintainer, daemonUser, normalizedName)

    val f = target / "Dockerfile"
    IO.write(f, dockerContent)
    f
  }

  def mapGenericFilesToDocker: Seq[Setting[_]] = {
    def renameDests(from: Seq[(File, String)], dest: String) = {
      for {
        (f, path) <- from
        newPath = "%s/%s" format (dest, path)
      } yield (f, newPath)
    }

    inConfig(Docker)(Seq(
      mappings <<= (mappings in Universal, defaultDockerInstallLocation) map { (mappings, dest) =>
        renameDests(mappings, dest)
      },
      mappings <++= dockerPackageMappings
    ))
  }

  def dockerSettings: Seq[Setting[_]] = Seq(
    sourceDirectory in Docker <<= sourceDirectory apply (_ / "docker"),
    target in Docker <<= target apply (_ / "docker")
  ) ++ mapGenericFilesToDocker ++ inConfig(Docker)(Seq(
      daemonUser := "daemon",
      dockerBaseImage := "dockerfile/java",
      defaultDockerInstallLocation := "/opt/docker",
      dockerPackageMappings <<= (sourceDirectory in Docker) map { dir =>
        MappingsHelper contentOf dir
      },
      stage <<= dockerGenerateContext.dependsOn(dockerGenerateConfig),
      dockerGenerateContext <<= (cacheDirectory, mappings, target) map {
        (cacheDirectory, mappings, t) =>
          val contextDir = t / "files"
          stageFiles("docker")(cacheDirectory, contextDir, mappings)
          contextDir
      },
      dockerGenerateConfig <<=
        (dockerBaseImage, defaultDockerInstallLocation, maintainer in Docker, daemonUser in Docker, normalizedName in Docker, target in Docker) map {
          case (dockerBaseImage, baseDirectory, maintainer, daemonUser, normalizedName, target) =>
            generateDockerConfig(dockerBaseImage, baseDirectory, maintainer, daemonUser, normalizedName, target)
        }
    ))
}
