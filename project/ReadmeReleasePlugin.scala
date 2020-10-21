import sbt._
import sbt.Keys._
import sbt.complete._
import sbt.complete.DefaultParsers._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.{ReleasePlugin, Vcs}

import scala.sys.process._

/**
  * == ReadmeRelease Plugin ==
  *
  * Changes the version in the README.md during a release.
  */
object ReadmeReleasePlugin extends AutoPlugin {

  override def requires: Plugins = ReleasePlugin

  override def trigger = AllRequirements

  object autoImport {

    /**
      * Update the readme file during a release
      */
    val updateReadme = ReleaseStep(updateReadmeStep)

    /**
      * Commits the readme changes.
      */
    val commitReadme = ReleaseStep(commitReadmeStep)

  }

  private def updateReadmeStep(state: State): State = {
    val extracted = Project.extract(state)
    val releaseVersion = extracted.get(version)
    val base = extracted.get(baseDirectory)
    val readmeFile = base / "README.md"

    val versionRegex = """(\d{1,2}\.\d{1,2}\.\d{1,2})""".r
    val updatedReadmeContent = versionRegex.replaceAllIn(IO.read(readmeFile), releaseVersion)

    IO.write(readmeFile, updatedReadmeContent)

    state
  }

  private def commitReadmeStep(state: State): State = {
    val log = toProcessLogger(state)
    val base = vcs(state).baseDir
    val sign = Project.extract(state).get(releaseVcsSign)
    val signOff = Project.extract(state).get(releaseVcsSignOff)
    val readmeFile = base / "README.md"

    val relativePath = IO
      .relativize(base, readmeFile)
      .getOrElse(
        "Version file [%s] is outside of this VCS repository with base directory [%s]!" format (readmeFile, base)
      )

    vcs(state).add(relativePath) !! log
    val vcsAddOutput = (vcs(state).status !!).trim
    if (vcsAddOutput.isEmpty)
      state.log.info("README.md hasn't been changed.")
    else
      vcs(state).commit("Update release version in readme", sign, signOff) ! log

    state
  }

  /**
    * Extracts the used vcs.
    *
    * Copied from the sbt-release plugin.
    * @param state sbt state
    * @return vcs implementation
    */
  private def vcs(state: State): Vcs =
    Project
      .extract(state)
      .get(releaseVcs)
      .getOrElse(sys.error("Aborting release. Working directory is not a repository of a recognized VCS."))

  /**
    * Creates a ProcessLogger from the current sbt state.
    *
    * Copied from the sbt-release plugin.
    * @param state
    * @return a process logger
    */
  private def toProcessLogger(state: State): ProcessLogger =
    new ProcessLogger {
      override def err(s: => String): Unit = state.log.info(s)
      override def out(s: => String): Unit = state.log.info(s)
      override def buffer[T](f: => T): T = state.log.buffer(f)
    }

}
