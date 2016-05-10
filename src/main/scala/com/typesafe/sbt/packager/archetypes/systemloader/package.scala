package com.typesafe.sbt.packager.archetypes

import sbt._
import java.io.File
import java.net.URL

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

  private def in(loader: ServerLoader, name: String): String = loader.toString + "/" + name

  private def overrideFromFile(sourceDirectory: File, loader: ServerLoader, name: String): Option[URL] = {
    Option(sourceDirectory / "templates" / "systemloader" / loader.toString / name)
      .filter(_.exists)
      .map(_.toURI.toURL)
  }
}