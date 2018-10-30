import sbt._
import sbt.Keys._
import sbt.complete._
import sbt.complete.DefaultParsers._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.{ReleasePlugin, Vcs}

import scala.sys.process._

/**
  * == Changelog Plugin ==
  *
  * This plugins manages the `CHANGELOG.md` file via the `github_changelog_generator` tool.
  * Make sure to follow the instructions on the project page and `github_changelog_generator`
  * is available.
  *
  * You will also need a Github token during the release process. `github_changelog_generator`
  * supports environment files or username/password combinations, but at this point the ChangelogPlugin
  * doesn't support them. The plugin also relies on the sensible defaults from `github_changelog_generator`,
  * which means
  * - the project name and organization are picked up automatically
  * - the output file is named CHANGELOG.md
  * - the changelog file includes everything (issues, pull requests)
  * - generated for all available version tasks
  * - runs in verbose mode
  *
  * @see [[https://github.com/skywinder/github-changelog-generator]]
  * @see [[https://github.com/skywinder/github-changelog-generator/wiki/Advanced-change-log-generation-examples]]
  */
object ChangelogPlugin extends AutoPlugin {

  override def requires: Plugins = ReleasePlugin

  override def trigger = AllRequirements

  object autoImport {

    /**
      * Generates the changelog during a release
      */
    val generateReleaseChangelog = ReleaseStep(generateChangelogStep)

    /**
      * Commits the generated changelog.
      */
    val commitChangelog = ReleaseStep(commitChangelogStep)

    /**
      * Task that executes the `github_changelog_generator`.
      */
    val generateChangelog: InputKey[Unit] =
      inputKey[Unit]("executes the github_changelog_generator to update the CHANGELOG.MD")

    /**
      * Github token used for the changelog generation
      */
    val generateChangelogToken: SettingKey[Option[String]] =
      settingKey[Option[String]]("github token used for the changelog generation")
  }

  import autoImport._

  private case class GithubChangeLogParameters(token: String)

  private val githubChangeLogParser: Parser[GithubChangeLogParameters] = {
    (Space ~ token("--token") ~ Space ~> StringBasic).map(GithubChangeLogParameters)
  }

  override def projectSettings: Seq[Setting[_]] =
    Seq(generateChangelogToken := None, generateChangelog := {
      val log = streams.value.log
      val parameters = githubChangeLogParser.parsed
      Seq("github_changelog_generator", "--token", parameters.token) ! log match {
        case 0 => log.success("CHANGELOG.md updated successfully")
        case n => sys.error(s"Failed updating CHANGELOG.md. Process existed with status code $n")
      }
    })

  private def generateChangelogStep(state: State): State = {
    val extracted = Project.extract(state)
    val predefinedToken = extracted.get(generateChangelogToken)

    val githubToken = readToken(predefinedToken)

    val (newState, _) = extracted.runInputTask(generateChangelog, s" --token $githubToken", state)
    newState
  }

  private def commitChangelogStep(state: State): State = {
    val log = toProcessLogger(state)
    val base = vcs(state).baseDir
    val sign = Project.extract(state).get(releaseVcsSign)
    val signOff = Project.extract(state).get(releaseVcsSignOff)
    val changelogFile = base / "CHANGELOG.md"

    val relativePath = IO
      .relativize(base, changelogFile)
      .getOrElse(
        "Version file [%s] is outside of this VCS repository with base directory [%s]!" format (changelogFile, base)
      )

    vcs(state).add(relativePath) !! log
    val vcsAddOutput = (vcs(state).status !!).trim
    if (vcsAddOutput.isEmpty) {
      state.log.info("CHANGELOG.md hasn't been changed.")
    } else {
      vcs(state).commit("Update changelog", sign, signOff) ! log
    }

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
  private def toProcessLogger(state: State): ProcessLogger = new ProcessLogger {
    override def err(s: => String): Unit = state.log.info(s)
    override def out(s: => String): Unit = state.log.info(s)
    override def buffer[T](f: => T): T = state.log.buffer(f)
  }

  private def readToken(predefinedToken: Option[String]): String = 
    predefinedToken
      // https://github.com/github-changelog-generator/github-changelog-generator#github-token
      .orElse(sys.env.get("CHANGELOG_GITHUB_TOKEN"))
      // get it from std in
      .getOrElse(SimpleReader.readLine("Github token: ") match {
        case Some(input) if input.trim.isEmpty => sys.error("No token provided")
        case Some(input)                       => input
        case None                              => sys.error("No token provided")
     })

}
