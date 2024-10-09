package com.typesafe.sbt.packager.archetypes.scripts

/**
  * Models the main script configuration for the given project.
  */
sealed trait StartScriptMainClassConfig

/**
  * The project has no main class.
  */
case object NoMain extends StartScriptMainClassConfig

/**
  * The project has a single defined main class.
  *
  * @param mainClass
  *   project main entrypoint
  */
case class SingleMain(mainClass: String) extends StartScriptMainClassConfig

/**
  * The project has multiple main classes, but no explicit configured main entrypoint.
  *
  * @param mainClasses
  *   A non-empty list of main classes
  */
case class MultipleMains(mainClasses: Seq[String]) extends StartScriptMainClassConfig

/**
  * The project has multiple main classes and a defined main entrypoint.
  *
  * @param mainClass
  *   Explicitly defined main class
  * @param additional
  *   Other discovered main classes without the explicit main class
  */
case class ExplicitMainWithAdditional(mainClass: String, additional: Seq[String]) extends StartScriptMainClassConfig

object StartScriptMainClassConfig {

  /**
    * @param mainClass
    *   optional main class, e.g. from (Compile / mainClass).value
    * @param discoveredMainClasses
    *   all discovered main classes, e.g. from (Compile / discoveredMainClasses).value
    * @return
    *   A start script configuration
    */
  def from(mainClass: Option[String], discoveredMainClasses: Seq[String]): StartScriptMainClassConfig = {
    val additionalMainClasses = discoveredMainClasses.filterNot(mainClass == Some(_))
    (mainClass, additionalMainClasses) match {
      // only one main - create the default script
      case (Some(main), Seq()) => SingleMain(main)
      case (None, Seq(main))   => SingleMain(main)
      // main explicitly set and multiple discoveredMainClasses
      case (Some(main), additional) => ExplicitMainWithAdditional(main, additional)
      // no main class at all
      case (None, Seq()) => NoMain
      // multiple main classes and none explicitly set. Create start script for each class
      case (None, additional) => MultipleMains(additional)
    }
  }

}
