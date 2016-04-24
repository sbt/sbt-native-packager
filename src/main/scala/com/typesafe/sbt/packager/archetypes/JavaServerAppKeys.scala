package com.typesafe.sbt.packager.archetypes

import sbt._

/**
 * Available settings/tasks for the [[com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging]]
 */
trait JavaServerAppKeys {

  val daemonStdoutLogFile = SettingKey[Option[String]]("daemon-stdout-log-file", "Filename for redirecting stdout/stderr output from daemon")

}
