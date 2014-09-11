package com.typesafe.sbt.packager.archetypes

import java.io.File
import java.net.URL

object ServerLoader extends Enumeration {
  type ServerLoader = Value
  val Upstart = Value("upstart")
  val SystemV = Value("systemv")
  val Systemd = Value("systemd")
}
