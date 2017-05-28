package com.typesafe.sbt
package packager
package debian

import sbt._
import sbt.Keys._
import SbtNativePackager._
import packager.Keys._

import java.net.HttpURLConnection
import javax.xml.bind.DatatypeConverter.printBase64Binary

trait DebianArtifactoryDeployKeys {
  val debianArtifactoryUrl = SettingKey[String]("debian-artifactory-url", " Url of Atrifactory server")
  val debianArtifactoryRepo = SettingKey[String]("debian-artifactory-repo", "Name of Artifactory repository with deb layout to publish artifacts to")
  val debianArtifactoryCredentials = SettingKey[Option[Credentials]]("debian-artifactory-credentials", "Credentials with permissions to publish to Artifactory server")

  val debianArtifactoryPath = SettingKey[String]("debian-artifactory-path", "The path in repository where the package should be stored")

  val debianArtifactoryDistribution = SettingKey[Seq[String]]("debian-artifactory-distribution", "The value to assign to the deb.distribution property used to specify the Debian package distribution")
  val debianArtifactoryComponent = SettingKey[Seq[String]]("debian-artifactory-component", "The value to assign to the deb.component property used to specify the Debian package component name")
  val debianArtifactoryArchitecture = SettingKey[Seq[String]]("debian-artifactory-architecture", "The value to assign to the deb.architecture property used to specify the Debian package architecture")

  val debianArtifactoryTargetPath = TaskKey[String]("debian-artifactory-apt-target-path", "Construct Atrifactory specific url path used for publishing")
  val debianArtifactoryPublish = TaskKey[Unit]("debian-artifactory-publish", "Publish debian package to Artifactory")
}

object DebianArtifactoryDeployPlugin extends AutoPlugin {

  override def requires = DebianPlugin

  object autoImport extends DebianArtifactoryDeployKeys

  import autoImport._

  override def projectSettings = inConfig(Debian)(Seq(
    debianArtifactoryArchitecture := Seq((packageArchitecture in Debian).value),
    debianArtifactoryTargetPath <<= (debianArtifactoryPath, debianArtifactoryDistribution, debianArtifactoryComponent, debianArtifactoryArchitecture) map makeTargetPath,
    debianArtifactoryPublish <<= (debianArtifactoryUrl, debianArtifactoryRepo, debianArtifactoryTargetPath, packageBin in Debian, debianArtifactoryCredentials, streams) map publishToArtifactory
  ))

  private def makeTargetPath(repoPath: String, distribution: Seq[String], component: Seq[String], arch: Seq[String]): String = {
    val dists = distribution.map(d => s"deb.distribution=$d")
    val comps = component.map(c => s"deb.component=$c")
    val archs = arch.map(a => s"deb.architecture=$a")
    (repoPath +: dists ++: comps ++: archs).mkString(";")
  }

  private def publishToArtifactory(artUrl: String, repo: String, targetPath: String, pkg: File, creds: Option[Credentials], streams: TaskStreams): Unit = {
    val putTo = url(s"$artUrl/$repo/$targetPath")

    val connection = putTo.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("PUT")
    connection.setDoOutput(true)

    Credentials.forHost(creds.toSeq, putTo.getHost) match {
      case None if creds.isEmpty =>
        streams.log.info(s"Artifactory credentials weren't supplied, proceeding without authentication")
      case None if creds.isDefined =>
        streams.log.warn(s"Couldn't find corresponding credentials for Artifactory host ${putTo.getHost}, proceeding without authentication")
      case Some(dc) =>
        streams.log.info(s"Found credentials for Artifactory host ${putTo.getHost}, proceeding with Basic authentication")
        val userpass = s"${dc.userName}:${dc.passwd}"
        val authHeader = "Basic " + printBase64Binary(userpass.getBytes)
        connection.setRequestProperty("Authorization", authHeader)
    }

    streams.log.info(s"Publishing package ${pkg.name} to $putTo")

    connection.connect()
    IO.transfer(pkg, connection.getOutputStream)
    connection.getOutputStream.flush()
    connection.getOutputStream.close()

    if (connection.getResponseCode == HttpURLConnection.HTTP_OK || connection.getResponseCode == HttpURLConnection.HTTP_CREATED) {
      val response = IO.readStream(connection.getInputStream)
      connection.getInputStream.close()
      streams.log.info(s"Package published:\n$response")
    } else {
      streams.log.error(s"Publish failed: ${connection.getResponseMessage}")
    }
    connection.disconnect()
  }

}
