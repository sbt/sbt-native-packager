package com.typesafe.sbt.packager.archetypes

import java.io.File
import java.net.URL

/**
 * Constructs an start script for running a java application.
 * Can build the neccessary maintainer scripts, too.
 */
object JavaAppStartScript {

  import ServerLoader._

  def apply(name: String, archetype: String, loader: ServerLoader, template: Option[File]): URL = {
    template flatMap { file =>
      if (file.exists) Some(file.toURI.toURL) else None
    } getOrElse {
      val path = archetype + "/systemloader/" + loader.toString + "/" + name
      loader.getClass.getResource(path)
    }
  }
}

object ServerLoader extends Enumeration {
  type ServerLoader = Value
  val Upstart = Value("upstart")
  val SystemV = Value("systemv")
  val Systemd = Value("systemd")
}
