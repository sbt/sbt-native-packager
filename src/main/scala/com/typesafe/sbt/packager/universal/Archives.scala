package com.typesafe.sbt
package packager
package universal

import sbt._

/** Helper methods to package up files into compressed archives. */
object Archives {

  /**
    * Makes a zip file in the given target directory using the given name.
    * @param target folder to build package in
    * @param name of output (without extension)
    * @param mappings included in the output
    * @param top level directory
    * @return zip file
    */
  @deprecated(
    "Use [[com.typesafe.sbt.packager.universal.Archives.makeZip(File, String, Seq[(File, String)], Option[String], Seq[String]): File]]",
    since = "1.0.5"
  )
  def makeZip(target: File, name: String, mappings: Seq[(File, String)], top: Option[String]): File =
    makeZip(target, name, mappings, top, options = Seq.empty)

  /**
    * Makes a zip file in the given target directory using the given name.
    *
    * @param target folder to build package in
    * @param name of output (without extension)
    * @param mappings included in the output
    * @param top level directory
    * @param options  NOT USED
    * @return zip file
    */
  def makeZip(
    target: File,
    name: String,
    mappings: Seq[(File, String)],
    top: Option[String],
    options: Seq[String]
  ): File = {
    val zip = target / (name + ".zip")

    // add top level directory if defined
    val m2 = top map { dir =>
      mappings map { case (f, p) => f -> (dir + "/" + p) }
    } getOrElse (mappings)

    ZipHelper.zip(m2, zip)
    zip
  }

  /**
    * Makes a zip file in the given target directory using the given name.
    *
    * @param target folder to build package in
    * @param name of output (without extension)
    * @param mappings included in the output
    * @param top level directory
    * @return zip file
    */
  @deprecated(
    "Use [[com.typesafe.sbt.packager.universal.Archives.makeNativeZip(File, String, Seq[(File, String)], Option[String], Seq[String]): File]]",
    since = "1.0.5"
  )
  def makeNativeZip(target: File, name: String, mappings: Seq[(File, String)], top: Option[String]): File =
    makeNativeZip(target, name, mappings, top, options = Seq.empty)

  /**
    * Makes a zip file in the given target directory using the given name.
    *
    * @param target folder to build package in
    * @param name of output (without extension)
    * @param mappings included in the output
    * @param top level directory
    * @param options  NOT USED
    * @return zip file
    */
  def makeNativeZip(
    target: File,
    name: String,
    mappings: Seq[(File, String)],
    top: Option[String],
    options: Seq[String]
  ): File = {
    val zip = target / (name + ".zip")

    // add top level directory if defined
    val m2 = top map { dir =>
      mappings map { case (f, p) => f -> (dir + "/" + p) }
    } getOrElse (mappings)

    ZipHelper.zipNative(m2, zip)
    zip
  }

  /**
    * Makes a dmg file in the given target directory using the given name.
    *
    *  Note:  Only works on macOS
    *
    *  @param target folder to build package in
    *  @param name of output (without extension)
    *  @param mappings included in the output
    *  @param top level directory : NOT USED
    *  @return dmg file
    */
  @deprecated(
    "Use [[com.typesafe.sbt.packager.universal.Archives.makeDmg(target: File, name: String, mappings: Seq[(File, String)], top: Option[String], options: Seq[String]): File]]",
    since = "1.0.5"
  )
  def makeDmg(target: File, name: String, mappings: Seq[(File, String)], top: Option[String]): File =
    makeDmg(target, name, mappings, top, options = Seq.empty)

  /**
    * Makes a dmg file in the given target directory using the given name.
    *
    *  Note:  Only works on macOS
    *
    *  @param target folder to build package in
    *  @param name of output (without extension)
    *  @param mappings included in the output
    *  @param top level directory : NOT USED
    *  @param options  NOT USED
    *  @return dmg file
    */
  def makeDmg(
    target: File,
    name: String,
    mappings: Seq[(File, String)],
    top: Option[String],
    options: Seq[String]
  ): File = {
    val t = target / "dmg"
    val dmg = target / (name + ".dmg")
    if (!t.isDirectory) IO.createDirectory(t)
    val sizeBytes =
      mappings.map(_._1).filterNot(_.isDirectory).map(_.length).sum
    // We should give ourselves a buffer....
    val neededMegabytes = math.ceil((sizeBytes * 1.05) / (1024 * 1024)).toLong

    // Create the DMG file:
    sys.process
      .Process(
        Seq("hdiutil", "create", "-megabytes", "%d" format neededMegabytes, "-fs", "HFS+", "-volname", name, name),
        Some(target)
      )
      .! match {
      case 0 => ()
      case n => sys.error("Error creating dmg: " + dmg + ". Exit code " + n)
    }

    // Now mount the DMG.
    val mountPoint = (t / name)
    if (!mountPoint.isDirectory) IO.createDirectory(mountPoint)
    val mountedPath = mountPoint.getAbsolutePath
    sys.process
      .Process(Seq("hdiutil", "attach", dmg.getAbsolutePath, "-readwrite", "-mountpoint", mountedPath), Some(target))
      .! match {
      case 0 => ()
      case n => sys.error("Unable to mount dmg: " + dmg + ". Exit code " + n)
    }

    // Now copy the files in
    val m2 = mappings map { case (f, p) => f -> (mountPoint / p) }
    IO.copy(m2)
    // Update for permissions
    for {
      (from, to) <- m2
      if from.canExecute()
    } to.setExecutable(true, true)

    // Now unmount
    sys.process.Process(Seq("hdiutil", "detach", mountedPath), Some(target)).! match {
      case 0 => ()
      case n =>
        sys.error("Unable to dismount dmg: " + dmg + ". Exit code " + n)
    }
    // Delete mount point
    IO.delete(mountPoint)
    dmg
  }

  /**
    * GZips a file.  Returns the new gzipped file.
    * NOTE: This will 'consume' the input file.
    */
  def gzip(f: File): File = {
    val par = f.getParentFile
    sys.process.Process(Seq("gzip", "-9", f.getAbsolutePath), Some(par)).! match {
      case 0 => ()
      case n => sys.error("Error gziping " + f + ". Exit code: " + n)
    }
    file(f.getAbsolutePath + ".gz")
  }

  /**
    * xz compresses a file.  Returns the new xz compressed file.
    * NOTE: This will 'consume' the input file.
    */
  def xz(f: File): File = {
    val par = f.getParentFile
    sys.process.Process(Seq("xz", "-S", ".xz", f.getAbsolutePath), Some(par)).! match {
      case 0 => ()
      case n => sys.error("Error xz-ing " + f + ". Exit code: " + n)
    }
    file(f.getAbsolutePath + ".xz")
  }

  val makeTxz = makeTarballWithOptions(xz, ".txz") _
  val makeTgz = makeTarballWithOptions(gzip, ".tgz") _

  /**
    * Helper method used to construct tar-related compression functions with `--force-local` and `-pvcf` option specified
    * as default.
    * @param target folder to build package in
    * @param name of output (without extension)
    * @param mappings included in the output
    * @param top level directory
    * @return tar file
    */
  def makeTarball(
    compressor: File => File,
    ext: String
  )(target: File, name: String, mappings: Seq[(File, String)], top: Option[String]): File =
    makeTarballWithOptions(compressor, ext)(target, name, mappings, top, options = Seq("--force-local", "-pcvf"))

  /**
    * Helper method used to construct tar-related compression functions.
    * @param target folder to build package in
    * @param name of output (without extension)
    * @param mappings included in the output
    * @param topDirectory level directory
    * @param options for tar command
    * @return tar file
    */
  def makeTarballWithOptions(compressor: File => File, ext: String)(
    target: File,
    name: String,
    mappings: Seq[(File, String)],
    topDirectory: Option[String],
    options: Seq[String]
  ): File = {
    val tarball = target / (name + ext)
    IO.withTemporaryDirectory { tempDirectory =>
      val workingDirectory = tempDirectory / name
      val temporaryMappings = topDirectory
        .map { dir =>
          mappings map { case (f, p) => f -> (workingDirectory / dir / p) }
        }
        .getOrElse {
          mappings map { case (f, p) => f -> (workingDirectory / p) }
        }

      // create the working directory
      IO.createDirectory(workingDirectory)
      IO.copy(temporaryMappings)
      // setExecutable does not always work. There are known issues with macOS where
      // the executable flags is missing after compression.
      for ((from, to) <- temporaryMappings if (to.getAbsolutePath contains "/bin/") || from.canExecute) {
        println("Making " + to.getAbsolutePath + " executable")
        to.setExecutable(true, false)
      }

      IO.createDirectory(tarball.getParentFile)

      // all directories that should be zipped
      val distdirs = topDirectory.map(_ :: Nil).getOrElse {
        IO.listFiles(workingDirectory).map(_.getName).toList // no top level dir, use all available
      }

      val temporaryTarFile = tempDirectory / (name + ".tar")

      val cmd = Seq("tar") ++ options ++ Seq(temporaryTarFile.getAbsolutePath) ++ distdirs
      println("Running with " + cmd.mkString(" "))
      sys.process.Process(cmd, workingDirectory).! match {
        case 0 => ()
        case n =>
          sys.error("Error tarballing " + tarball + ". Exit code: " + n)
      }
      IO.copyFile(compressor(temporaryTarFile), tarball)
    }
    tarball
  }
}
