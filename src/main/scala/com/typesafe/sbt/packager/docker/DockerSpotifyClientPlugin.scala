package com.typesafe.sbt.packager.docker

import java.nio.file.Paths

import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.stage

import sbt.Keys._
import sbt._

/**
  * == DockerSpotifyClientPlugin Plugin ==
  *
  * This plugin helps you build docker containers using Spotify Docker Client.
  *
  * == Configuration ==
  *
  * In order to configure this plugin take a look at the available [[com.typesafe.sbt.packager.docker.DockerKeys]]
  *
  * == Requirements ==
  *
  * You need docker to have docker installed on your system.
  * Check with a single command:
  *
  * {{{
  * docker version
  * }}}
  *
  * @note this plugin is not intended to build very customizable docker images, but turn your mappings
  *       configuration in a docker image with almost no ''any'' configuration.
  *
  * @example Enable the plugin in the `build.sbt`
  * {{{
  *   enablePlugins(DockerSpotifyClientPlugin)
  * }}}
  *
  * and add the dependency in your `plugins.sbt`
  *
  * {{{
  *   libraryDependencies += "com.spotify" % "docker-client" % "3.5.13"
  * }}}
  *
  * The Docker-spotify client is a provided dependency so you have to add it on your own.
  * It brings a lot of dependenciesthat could slow your build times. This is the reason
  * the dependency is marked as provided.
  */
object DockerSpotifyClientPlugin extends AutoPlugin {

  override def requires: Plugins = DockerPlugin

  import DockerPlugin.autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = inConfig(Docker)(clientSettings)

  def clientSettings =
    Seq(
      publishLocal := publishLocalDocker.value,
      dockerVersion := dockerServerVersion.value,
      dockerApiVersion := dockerServerApiVersion.value
    )

  def publishLocalDocker: Def.Initialize[Task[Unit]] =
    Def.task {
      val context = stage.value
      val primaryAlias = dockerAlias.value
      val aliases = dockerAliases.value
      val log = streams.value.log

      val dockerDirectory = context.toString

      val docker = new DockerClientTask()
      docker.packageDocker(primaryAlias, aliases, dockerDirectory, log)
    }

  def dockerServerVersion: Def.Initialize[Task[Option[DockerVersion]]] =
    Def.task {
      val docker = new DockerClientTask()
      docker.dockerServerVersion()
    }

  def dockerServerApiVersion: Def.Initialize[Task[Option[DockerApiVersion]]] =
    Def.task {
      val docker = new DockerClientTask()
      docker.dockerServerApiVersion()
    }

}

/**
  * == Docker Client Task ==
  *
  * This private class contains all the docker-plugin specific implementations. It's only invoked when the docker
  * plugin is enabled and the `docker:publishLocal` task is called. This means that all classes in
  * `com.spotify.docker.client._` are only loaded when required and allows us to put the dependency in the "provided"
  * scope. The provided scope means that we have less dependency issues in an sbt build.
  */
private class DockerClientTask {
  import com.spotify.docker.client.DockerClient.BuildParam
  import com.spotify.docker.client.messages.ProgressMessage
  import com.spotify.docker.client.{DefaultDockerClient, DockerClient, ProgressHandler}

  def packageDocker(
    primaryAlias: DockerAlias,
    aliases: Seq[DockerAlias],
    dockerDirectory: String,
    log: Logger
  ): Unit = {
    val docker: DockerClient = DefaultDockerClient.fromEnv().build()

    log.info(s"PublishLocal using Docker API ${docker.version().apiVersion()}")

    docker.build(
      Paths.get(dockerDirectory),
      primaryAlias.toString,
      new ProgressHandler {
        override def progress(message: ProgressMessage): Unit =
          Option(message.error()) match {
            case Some(error) if error.nonEmpty => log.error(message.error())
            case _                             => Option(message.stream()) foreach (v => log.info(v))
          }
      },
      BuildParam.forceRm()
    )

    aliases.foreach { tag =>
      docker.tag(primaryAlias.toString, tag.toString, true)
    }
  }

  def dockerServerVersion(): Option[DockerVersion] = {
    val docker: DockerClient = DefaultDockerClient.fromEnv().build()
    DockerVersion.parse(docker.version().version())
  }

  def dockerServerApiVersion(): Option[DockerApiVersion] = {
    val docker: DockerClient = DefaultDockerClient.fromEnv().build()
    DockerApiVersion.parse(docker.version().apiVersion())
  }
}
