package com.typesafe.sbt.packager.windows

// TODO find a better name and add documentation
object NameHelper {

  def makeEnvFriendlyName(name: String): String = name.toUpperCase.replaceAll("\\W", "_")
}
