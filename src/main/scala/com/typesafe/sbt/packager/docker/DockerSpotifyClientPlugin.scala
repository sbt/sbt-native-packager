package com.typesafe.sbt
package packager
package docker

import java.nio.file.Paths

import com.spotify.docker.client.messages.ProgressMessage
import com.spotify.docker.client.{ProgressHandler, DockerClient, DefaultDockerClient}
import com.spotify.docker.client.DockerClient.BuildParameter._
import sbt._
import sbt.Keys._
import packager.Keys._
import universal.UniversalPlugin.autoImport.stage


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
 *
 * @note this plugin is not intended to build very customizable docker images, but turn your mappings
 *       configuration in a docker image with almost no ''any'' configuration.
 *
 * @example Enable the plugin in the `build.sbt`
 *          {{{
 *              enablePlugins(DockerSpotifyClientPlugin)
 *          }}}
 */
object DockerSpotifyClientPlugin extends AutoPlugin {

  override def requires = DockerPlugin

  import DockerPlugin.autoImport._

  override lazy val projectSettings = inConfig(Docker)(clientSettings)

  def clientSettings = Seq(
    publishLocal := publishLocalDocker.value

  )

  def publishLocalDocker = Def.task {
    val context = stage.value
    val tag = dockerTarget.value
    val latest = dockerUpdateLatest.value
    val log = streams.value.log

    val dockerDirectory = context.toString
    val docker: DockerClient = DefaultDockerClient.fromEnv().build()

    log.info(s"PublishLocal using Docker API ${docker.version().apiVersion()}")

    val id = docker.build(Paths.get(dockerDirectory), tag, new ProgressHandler() {
      def progress(message: ProgressMessage) = {
        Option(message.error()) match {
          case Some(error) if error.nonEmpty => log.error(message.error())
          case _ => log.info(message.stream())
        }
      }
    }, FORCE_RM)

    if (latest) {
      val name = tag.substring(0, tag.lastIndexOf(":")) + ":latest"
      docker.tag(tag, name, true)
    }
  }


}
