package com.typesafe.sbt.packager.archetypes.systemloader

/**
  * Stores the available types of server loaders.
  *
  * @note
  *   not all packaging systems support all server loaders
  */
object ServerLoader extends Enumeration {
  type ServerLoader = Value
  val Upstart = Value("upstart")
  val SystemV = Value("systemv")
  val Systemd = Value("systemd")
}
