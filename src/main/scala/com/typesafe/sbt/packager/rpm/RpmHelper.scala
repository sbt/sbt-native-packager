package com.typesafe.sbt.packager.rpm

import sbt._
import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.linux.LinuxSymlink

object RpmHelper {

  /** Returns the host vendor for an rpm. */
  def hostVendor =
    sys.process.Process(Seq("rpm", "-E", "%{_host_vendor}")) !!

  /**
    * Prepares the staging directory for the rpm build command.
    *
    * @param spec The RpmSpec
    * @param workArea The target
    * @param log Logger
    * @return the `workArea`
    */
  def stage(spec: RpmSpec, workArea: File, log: sbt.Logger): File = {
    buildWorkArea(workArea)
    copyFiles(spec, workArea, log)
    writeSpecFile(spec, workArea, log)
    spec.validate(log)
    workArea
  }

  /**
    * Build the rpm package
    *
    * @param spec The RpmSpec
    * @param stagingArea Prepared staging area
    * @param log Logger
    * @return The rpm package
    */
  def buildRpm(spec: RpmSpec, stagingArea: File, log: sbt.Logger): File = {
    buildPackage(stagingArea, spec, log)
    // We should probably return the File that was created.
    val rpmname = "%s-%s-%s.%s.rpm" format (spec.meta.name, spec.meta.version, spec.meta.release, spec.meta.arch)
    stagingArea / "RPMS" / spec.meta.arch / rpmname
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

    LinuxSymlink.makeSymLinks(spec.symlinks, buildroot, relativeLinks = false)
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

  private[this] def buildPackage(workArea: File, spec: RpmSpec, log: sbt.Logger): Unit = {
    val buildRoot = workArea / "buildroot"
    val specsDir = workArea / "SPECS"
    val gpg = false
    // TODO - Full GPG support (with GPG plugin).
    IO.withTemporaryDirectory { tmpRpmBuildDir =>
      val args: Seq[String] = (spec.setarch match {
        case Some(arch) => Seq("setarch", arch)
        case None       => Seq()
      }) ++ Seq(
        "rpmbuild",
        "-bb",
        "--target",
        spec.meta.arch + '-' + spec.meta.vendor + '-' + spec.meta.os,
        "--buildroot",
        buildRoot.getAbsolutePath,
        "--define",
        "_topdir " + workArea.getAbsolutePath,
        "--define",
        "_tmppath " + tmpRpmBuildDir.getAbsolutePath
      ) ++ (
        if (gpg) Seq("--define", "_gpg_name " + "<insert keyname>", "--sign")
        else Seq.empty
      ) ++ Seq(spec.meta.name + ".spec")
      log.debug("Executing rpmbuild with: " + args.mkString(" "))
      // RPM outputs to standard error in non-error cases. So just collect all the output, then dump
      // it all to either error log or info log depending on the exit status
      val outputBuffer = collection.mutable.ArrayBuffer.empty[String]
      sys.process.Process(args, Some(specsDir)) ! sys.process.ProcessLogger(o => outputBuffer.append(o)) match {
        case 0 =>
          // Workaround for #1246 - random tests fail with a NullPointerException in the sbt ConsoleLogger
          // I wasn't able to reproduce this locally and there aren't any user reports on this, so we catch
          // the NPE and log via println
          try {
            outputBuffer.foreach(log.info(_))
          } catch {
            case e: NullPointerException =>
              outputBuffer.foreach(println(_))
          }
        case code =>
          outputBuffer.foreach(log.error(_))
          sys.error("Unable to run rpmbuild, check output for details. Errorcode " + code)
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
    sys.process.Process(Seq("rpm", "--eval", '%' + mcro)).!!
}
