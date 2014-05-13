import sbt._
import Keys._

import complete.DefaultParsers._
import complete.Parser

object Release {

  val versionNumberParser: Parser[String] = {
    val classifier: Parser[String] = ("-" ~ ID) map {
      case (dash, id) => dash + id
    }
    val version: Parser[String] = (Digit ~ chars(".0123456789").* ~ classifier.?) map {
      case ((first, rest), optClass) => ((first +: rest).mkString + optClass.getOrElse(""))
    }
    val complete = (chars("v") ~ token(version, "<version number>")) map {
      case (v, num) => v + num
    }
    complete  
  }

  def releaseParser(state: State): Parser[String] =
    Space ~> versionNumberParser


  val releaseHelp = Help("release",
    "release <git tag>" -> "Runs the release script for a given version number",
    """|release <git tag>
       |
       |Runs our release script.  This will:
       |1. Run all the tests (unit + scripted) for the current OS.
       |2. Tag the git repo with the given tag (v<version>).
       |3. Reload the build with the new version number from the git tag.
       |4. publish all the artifacts to bintray.""".stripMargin
  )

  def scriptedForPlatform: String = {
  	// TODO - Implement.   Instead of only running tests we can, we should 
  	// probably ping some service to see if all platform tests have
  	// succeeded.
  	"scripted universal/* debian/* rpm/*"
  }

  def releaseAction(state: State, tag: String): State = {
    // TODO - Ensure we're releasing on JDK 6, so we're binary compatible.
    // First check to ensure we have a sane publishing environment...
    "bintrayCheckCredentials" ::
    "+ test" ::
    // Workaround for 0.12.4 scripted issue
    "set scalaVersion in Global := \"2.10.2\"" ::
    scriptedForPlatform ::
    // TODO - Signed tags, possibly using pgp keys?
    ("git tag " + tag) ::
    "reload" ::
    // TODO - once we figure out bintray + pubishSigned, switch to signed
    // releases.
    "+ publishSigned" ::
    "bintrayPublishAllStaged" ::
    ("git push origin " + tag) ::
  	state
  }

  val releaseCommand = 
    Command("release", releaseHelp)(releaseParser)(releaseAction)

  def settings: Seq[Setting[_]]=
    Seq(commands += releaseCommand)
}