package com.typesafe.sbt.packager.archetypes.systemloader

import sbt._
import sbt.Keys.{target, sourceDirectory}
import com.typesafe.sbt.packager.Keys.{
  maintainerScripts,
  packageName,
  linuxStartScriptName,
  linuxStartScriptTemplate,
  linuxMakeStartScript,
  linuxScriptReplacements,
  linuxPackageMappings,
  defaultLinuxStartScriptLocation
 }
import com.typesafe.sbt.packager.archetypes.MaintainerScriptHelper.maintainerScriptsAppend
import com.typesafe.sbt.packager.archetypes.ServerLoader._
import com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd
import com.typesafe.sbt.packager.linux._
import com.typesafe.sbt.packager.linux.LinuxPlugin.Users
import com.typesafe.sbt.packager.linux.LinuxPlugin.autoImport.{Linux}
import com.typesafe.sbt.packager.debian.DebianPlugin
import com.typesafe.sbt.packager.debian.DebianPlugin.autoImport.{ Debian, DebianConstants }
import com.typesafe.sbt.packager.rpm.RpmPlugin
import com.typesafe.sbt.packager.rpm.RpmPlugin.autoImport.{Rpm, RpmConstants}
import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport.{Universal}

import java.nio.file.{ Paths, Files }

object SystemD extends AutoPlugin {

  override def requires = DebianPlugin && RpmPlugin

  object autoImport extends Keys {
    // all systemd specific settings/tasks here
    val systemdExitSuccessStatus = settingKey[Int]("defines the ExitSuccessStatus for systemd")
  }

  import autoImport._

  override def projectSettings: Seq[Setting[_]] = debianSettings ++ rpmSettings

  def debianSettings: Seq[Setting[_]] = Seq(
    linuxStartScriptName in Debian := Some((packageName in Debian).value + ".service"),
    defaultLinuxStartScriptLocation := "/usr/lib/systemd/system",
    // add loader-functions to script replacements
	  linuxScriptReplacements in Debian += loaderFunctionsReplacement((sourceDirectory in Compile).value, Systemd),
	  // set the template
    linuxStartScriptTemplate in Debian := linuxStartScriptUrl((sourceDirectory in Compile).value, Systemd),
    // define task to generate the systemloader script
    linuxMakeStartScript := makeStartScript(
        (linuxStartScriptTemplate in Debian).value,
        (linuxScriptReplacements in Debian).value,
        (target in Universal).value,
        "systemd-init"
    ),
    // add systemloader to mappings. TODO generalize this
    linuxPackageMappings in Debian ++= startScriptMapping(
    		(linuxStartScriptName in Debian).value,
        (linuxMakeStartScript in Debian).value,
        (defaultLinuxStartScriptLocation in Debian).value
    ),
    // add automatic service start/stop
    maintainerScripts in Debian := maintainerScriptsAppend(
       (maintainerScripts in Debian).value,
       (linuxScriptReplacements in Debian).value
    )(
        DebianConstants.Postinst -> """|# SystemD support
                                       |${{loader-functions}}
                                       |startService ${{app_name}} || echo "${{app_name}} could not be registered or started"
                                       |""".stripMargin,
        DebianConstants.Prerm -> """|# SystemD support
                                    |${{loader-functions}}
                                    |stopService ${{app_name}} || echo "${{app_name}} wasn't even running!"
                                    |""".stripMargin
    )
  )

  def rpmSettings: Seq[Setting[_]] = Seq(
          linuxStartScriptName in Rpm := Some((packageName in Rpm).value + ".service"),
    defaultLinuxStartScriptLocation := "/usr/lib/systemd/system",
    // add loader-functions to script replacements
	  linuxScriptReplacements in Rpm += loaderFunctionsReplacement((sourceDirectory in Compile).value, Systemd),
	  // set the template
    linuxStartScriptTemplate in Rpm := linuxStartScriptUrl((sourceDirectory in Compile).value, Systemd),
    // define task to generate the systemloader script
    linuxMakeStartScript := makeStartScript(
        (linuxStartScriptTemplate in Rpm).value,
        (linuxScriptReplacements in Rpm).value,
        (target in Universal).value,
        "systemd-init"
    ),
    // add systemloader to mappings. TODO generalize this
    linuxPackageMappings in Rpm ++= startScriptMapping(
    		(linuxStartScriptName in Rpm).value,
        (linuxMakeStartScript in Rpm).value,
        (defaultLinuxStartScriptLocation in Debian).value
    ),
    // add automatic service start/stop
    maintainerScripts in Rpm := maintainerScriptsAppend(
        (maintainerScripts in Rpm).value,
        (linuxScriptReplacements in Rpm).value
    )(
        RpmConstants.Post -> """|# SystemD support
                                |${{loader-functions}}
                                |# Scriptlet syntax: http://fedoraproject.org/wiki/Packaging:ScriptletSnippets#Syntax
                                |# $1 == 1 is first installation and $1 == 2 is upgrade
                                |if [ $1 -eq 1 ] ;
                                |then
                                |  startService ${{app_name}} || echo "Could not start ${{app_name}}"
                                |fi
                                |""".stripMargin,
        RpmConstants.Postun -> """|# SystemD support
                                  |if [ $1 -ge 1 ]
                                  |  restartService ${{app_name}} || echo "Failed to try-restart ${{app_name}}"
                                  |fi
                                  |""".stripMargin,
        RpmConstants.Preun -> """|# SystemD support
                                 |${{loader-functions}}
                                 |if [ $1 -eq 0 ] ;
                                 |then
                                 |  stopService ${{app_name}} || echo "Could not stop ${{app_name}}"
                                 |fi
                                 |""".stripMargin
    )
  )
  
  
  
  // TODO refactor this in a generic method for all systemloaders
  private def startScriptMapping(
      scriptName: Option[String], script: Option[File], location: String): Seq[LinuxPackageMapping] = {
    val name = scriptName.getOrElse(
        sys.error("""No linuxStartScriptName defined. Add `linuxStartScriptName in <PackageFormat> := Some("name.service")""")
    )
    val path = location + "/" + name
    for {
      s <- script.toSeq
    } yield LinuxPackageMapping(Seq(s -> path), LinuxFileMetaData(Users.Root, Users.Root, "0644", "true"))
  }
}
