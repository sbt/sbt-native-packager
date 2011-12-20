# SBT Native Packager #

This is a work in process project.  The goal is to be able to bundle up Scala software built with SBT for native packaging systems, like deb, rpm, homebrew, msi.

Here's an example build for the debian package of [sbt-extras](http://github.com/paulp/sbt-extras) project:

    val settings: Seq[Setting[_]] = debianSettings ++ Seq(
        maintainer in Debian := "Josh Suereth <joshua.suereth@typesafe.com>",
        name in Debian := "sbt",
        version in Debian <<= (version, sbtVersion) apply { (v, sv) =>       
          sv + "-build-" + (v split "\\." map (_.toInt) dropWhile (_ == 0) map ("%02d" format _) mkString "")
        },
        debianPackageDependencies in Debian ++= Seq("curl", "java2-runtime", "bash (>= 2.05a-11)"),
        debianPackageRecommends in Debian += "git",
        packageDescription in Debian := """Simple Build Tool
     This script provides a native way to run the Simple Build Tool,
     a build tool for Scala software, also called SBT.""",
        linuxPackageMappings in Debian <+= (baseDirectory) map { bd =>
          (packageForDebian((bd / "sbt") -> "usr/bin/sbt")
           withUser "root" withGroup "root" withPerms "0755")
        },
        linuxPackageMappings in Debian <+= (sourceDirectory in Debian) map { bd =>
          packageForDebian(
              (bd / "usr/share/doc/sbt/copyright") -> "usr/share/doc/sbt/copyright"
          ) withUser "root" withGroup "root" withPerms "0644"
        },   
        linuxPackageMappings in Debian <+= (sourceDirectory) map { bd =>
          packageForDebian(
            (bd / "debian/changelog") -> "usr/share/doc/sbt/changelog.gz",
            (bd / "linux" / "usr/share/man/man1/sbt.1") -> "usr/share/man/man1/sbt.1.gz"
          ) withUser "root" withGroup "root" withPerms "0644" gzipped
        }
      )


More to come shortly.
