package com.typesafe.sbt.packager.archetypes

import sbt._
import java.io.File
import java.net.URL

import com.typesafe.sbt.packager.linux._
import com.typesafe.sbt.packager.linux.LinuxPlugin.Users

import ServerLoader.ServerLoader

package object systemloader {

  private val LOADER_FUNCTIONS = "loader-functions"

  def linuxStartScriptUrl(sourceDirectory: File, loader: ServerLoader, name: String = "start-template"): URL = {
    overrideFromFile(sourceDirectory, loader, name)
      .getOrElse(getClass getResource in(loader, name))
  }

  def loaderFunctionsReplacement(sourceDirectory: File, loader: ServerLoader): (String, String) = {
    val source = overrideFromFile(sourceDirectory, loader, LOADER_FUNCTIONS)
      .orElse(Option(getClass getResource in(loader, LOADER_FUNCTIONS)))
      .getOrElse(sys.error("Loader functions could not be loaded"))
    LOADER_FUNCTIONS -> TemplateWriter.generateScript(source, Nil)
  }

  def makeStartScript(template: URL, replacements: Seq[(String, String)], tmpDir: File, name: String): Option[File] = {
    val scriptBits = TemplateWriter generateScript (template, replacements)
    val script = tmpDir / "tmp" / "systemloader" / name
    IO.write(script, scriptBits)
    Some(script)
  }

  /**
   * Create the linuxPackageMapping for the systemloader start-script/conffile
   * @param scriptName - optional name from `linuxStartScriptName.value`
   * @param script - file with contents from ` linuxMakeStartScript.value`
   * @param location - target destination from `defaultLinuxStartScriptLocation.value`
   */
  def startScriptMapping(
    scriptName: Option[String], script: Option[File], location: String): Seq[LinuxPackageMapping] = {
    val name = scriptName.getOrElse(
      sys.error("""No linuxStartScriptName defined. Add `linuxStartScriptName in <PackageFormat> := Some("name.service")""")
    )
    val path = location + "/" + name
    for {
      s <- script.toSeq
    } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(Users.Root, Users.Root, "0644", "true"))
  }

  private def in(loader: ServerLoader, name: String): String = loader.toString + "/" + name

  private def overrideFromFile(sourceDirectory: File, loader: ServerLoader, name: String): Option[URL] = {
    Option(sourceDirectory / "templates" / "systemloader" / loader.toString / name)
      .filter(_.exists)
      .map(_.toURI.toURL)
  }
}