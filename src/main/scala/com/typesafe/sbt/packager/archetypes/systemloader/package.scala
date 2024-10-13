package com.typesafe.sbt.packager.archetypes

import sbt._
import java.io.File
import java.net.URL

import com.typesafe.sbt.packager.linux._
import com.typesafe.sbt.packager.linux.LinuxPlugin.Users

import com.typesafe.sbt.packager.archetypes.systemloader.ServerLoader._

package object systemloader {

  private val LOADER_FUNCTIONS = "loader-functions"

  def linuxStartScriptUrl(
    sourceDirectory: File,
    loaderOpt: Option[ServerLoader],
    name: String = "start-template"
  ): URL = {
    val loader = loaderOpt.getOrElse(
      sys.error("No serverLoader defined. Enable a systemloader, e.g. with `enablePlugins(UpstartPlugin)`")
    )
    overrideFromFile(sourceDirectory, loader, name).getOrElse(getClass getResource in(loader, name))
  }

  def loaderFunctionsReplacement(sourceDirectory: File, loaderOpt: Option[ServerLoader]): (String, String) = {
    val replacement = for {
      loader <- loaderOpt
      source <- overrideFromFile(sourceDirectory, loader, LOADER_FUNCTIONS).orElse(
        Option(getClass getResource in(loader, LOADER_FUNCTIONS))
      )
    } yield LOADER_FUNCTIONS -> TemplateWriter.generateScript(source, Nil)

    replacement.getOrElse(sys.error(s"Loader functions could not be loaded for ${loaderOpt}"))
  }

  def makeStartScript(
    template: URL,
    replacements: Seq[(String, String)],
    target: File,
    path: String,
    name: String
  ): Option[File] = {
    val scriptBits = TemplateWriter generateScript (template, replacements)
    val script = target / "tmp" / path / name
    IO.write(script, scriptBits)
    Some(script)
  }

  /**
    * Create the linuxPackageMapping for the systemloader start-script/conffile
    * @param scriptName
    *   optional name from `linuxStartScriptName.value`
    * @param script
    *   file with contents from ` linuxMakeStartScript.value`
    * @param location
    *   target destination from `defaultLinuxStartScriptLocation.value`
    * @param isConf
    *   if the start script should be registered as a config file
    */
  def startScriptMapping(
    scriptName: Option[String],
    script: Option[File],
    location: String,
    isConf: Boolean
  ): Seq[LinuxPackageMapping] = {
    val name = scriptName.getOrElse(
      sys.error(
        """No linuxStartScriptName defined. Add `<PackageFormat> / linuxStartScriptName := Some("name.service")"""
      )
    )
    val path = location + "/" + name
    val perms = if (isConf) "0644" else "0755"
    for {
      s <- script.toSeq
    } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(Users.Root, Users.Root, perms, isConf.toString))
  }

  private def in(loader: ServerLoader, name: String): String =
    loader.toString + "/" + name

  private def overrideFromFile(sourceDirectory: File, loader: ServerLoader, name: String): Option[URL] =
    Option(sourceDirectory / "templates" / "systemloader" / loader.toString / name).filter(_.exists).map(_.toURI.toURL)
}
