package com.typesafe.sbt
package packager
package rpm

import sbt._
import com.typesafe.sbt.packager.linux.LinuxSymlink

object RpmHelper {

  /** Returns the host vendor for an rpm. */
  def hostVendor =
    Process(Seq("rpm", "-E", "%{_host_vendor}")) !!

  def buildRpm(spec: RpmSpec, workArea: File, log: sbt.Logger): File = {
    // TODO - check the spec for errors.
    buildWorkArea(workArea)
    copyFiles(spec, workArea, log)
    writeSpecFile(spec, workArea, log)

    buildPackage(workArea, spec, log)
    // We should probably return the File that was created.
    val rpmname = "%s-%s-%s.%s.rpm" format (spec.meta.name, spec.meta.version, spec.meta.release, spec.meta.arch)
    workArea / "RPMS" / spec.meta.arch / rpmname
  }

  private[this] def copyFiles(spec: RpmSpec, workArea: File, log: sbt.Logger): Unit = {
    // TODO - special treatment of icon...
    val buildroot = workArea / "tmp-buildroot"

    def copyWithZip(from: File, to: File, zipped: Boolean): Unit = {
      log.debug("Copying %s to %s".format(from, to))
      if (zipped) IO.gzip(from, to)
      else IO.copyFile(from, to, true)
    }
    // First make sure directories are there....
    IO createDirectories (for {
      mapping <- spec.mappings
      (file, dest) <- mapping.mappings
      if file.isDirectory
      target = buildroot / dest
    } yield target)

    // We don't have to do any permission modifications since that's in the
    // the .spec file.
    for {
      mapping <- spec.mappings
      (file, dest) <- mapping.mappings
      if file.exists && !file.isDirectory()
      target = buildroot / dest
    } copyWithZip(file, target, mapping.zipped)
  }

  private[this] def writeSpecFile(spec: RpmSpec, workArea: File, log: sbt.Logger): File = {
    val specdir = workArea / "SPECS"
    val rpmBuildroot = workArea / "buildroot"
    val tmpBuildRoot = workArea / "tmp-buildroot"
    val specfile = specdir / (spec.meta.name + ".spec")
    log.debug("Creating SPEC file: " + specfile.getAbsolutePath)
    IO.write(specfile, spec.writeSpec(rpmBuildroot, tmpBuildRoot))
    specfile
  }

  private[this] def buildPackage(
    workArea: File,
    spec: RpmSpec,
    log: sbt.Logger): Unit = {
    val buildRoot = workArea / "buildroot"
    val specsDir = workArea / "SPECS"
    val gpg = false
    // TODO - Full GPG support (with GPG plugin).
    IO.withTemporaryDirectory { tmpRpmBuildDir =>
      val args: Seq[String] = Seq(
        "rpmbuild",
        "-bb",
        "--target", spec.meta.arch + '-' + spec.meta.vendor + '-' + spec.meta.os,
        "--buildroot", buildRoot.getAbsolutePath,
        "--define", "_topdir " + workArea.getAbsolutePath,
        "--define", "_tmppath " + tmpRpmBuildDir.getAbsolutePath
      ) ++ (
          if (gpg) Seq("--define", "_gpg_name " + "<insert keyname>", "--sign")
          else Seq.empty
        ) ++ Seq(spec.meta.name + ".spec")
      log.debug("Executing rpmbuild with: " + args.mkString(" "))
      (Process(args, Some(specsDir)) ! log) match {
        case 0    => ()
        case code => sys.error("Unable to run rpmbuild, check output for details. Errorcode " + code)
      }
    }
  }

  private[this] val topleveldirs = Seq("BUILD", "RPMS", "SOURCES", "SPECS", "SRPMS", "tmp-buildroot", "buildroot")

  /** Builds the work area and returns the tmp build root, and rpm build root. */
  private[this] def buildWorkArea(workArea: File): Unit = {
    if (!workArea.exists) workArea.mkdirs()
    // TODO - validate workarea
    // Clean out work area
    topleveldirs map (workArea / _) foreach { d =>
      if (d.exists()) IO.delete(d)
      d.mkdir()
    }
  }

  def evalMacro(mcro: String): String =
    Process(Seq("rpm", "--eval", '%' + mcro)).!!
}
