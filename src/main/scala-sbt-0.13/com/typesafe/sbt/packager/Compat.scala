package com.typesafe.sbt.packager

import sbt.{Artifact, BufferedLogger, FullLogger, Logger}

import scala.sys.process.ProcessLogger

object Compat {

  /**
    * Used in:
    *
    * - [[com.typesafe.sbt.packager.windows.WindowsPlugin]]
    * - [[com.typesafe.sbt.packager.rpm.RpmHelper]]
    * - [[com.typesafe.sbt.packager.docker.DockerPlugin]]
    * - [[com.typesafe.sbt.packager.debian.DebianNativePackaging]]
    * - [[com.typesafe.sbt.packager.rpm.RpmPlugin]]
    *
    * @param log
    * @return turns a Logger into a ProcessLogger
    */
  implicit def log2ProcessLogger(log: Logger): sys.process.ProcessLogger =
    new BufferedLogger(new FullLogger(log)) with sys.process.ProcessLogger {
      def err(s: => String): Unit = error(s)
      def out(s: => String): Unit = info(s)
    }

  /**
    * Used in
    *
    * - [[com.typesafe.sbt.packager.docker.DockerPlugin]]
    *
    * @param logger The sbt.ProcessLogger that should be wrapped
    * @return A scala ProcessLogger
    */
  implicit def sbtProcessLogger2ScalaProcessLogger(logger: sbt.ProcessLogger): sys.process.ProcessLogger =
    ProcessLogger(msg => logger.info(msg), err => logger.error(err))

  /**
    * Use in the scripted `universal/multiproject-classifiers` test.
    * @param artifact polyfill new methods
    */
  implicit class CompatArtifact(artifact: Artifact) {
    def withClassifier(classifier: Option[String]): Artifact =
      artifact.copy(classifier = classifier)
  }
}
