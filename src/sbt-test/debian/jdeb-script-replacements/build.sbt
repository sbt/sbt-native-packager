enablePlugins(JavaServerAppPackaging, JDebPackaging)

name := "debian-test"

version := "0.1.0"

maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test debian package"

packageDescription := """A fun package description of our software,
  with multiple lines."""

debianPackageDependencies in Debian ++= Seq("java2-runtime",
                                            "bash (>= 2.05a-11)")

debianPackageRecommends in Debian += "git"

TaskKey[Unit]("check-control-files") <<= (target, streams) map {
  (target, out) =>
    val header = "#!/bin/sh"
    val extracted = target / "extracted"
    println(extracted.getAbsolutePath)
    Seq("dpkg-deb",
        "-R",
        (target / "debian-test_0.1.0_all.deb").absolutePath,
        extracted.absolutePath).!
    val preinst = extracted / "DEBIAN/preinst"
    val postinst = extracted / "DEBIAN/postinst"
    val prerm = extracted / "DEBIAN/prerm"
    val postrm = extracted / "DEBIAN/postrm"
    Seq(preinst, postinst, prerm, postrm) foreach { script =>
      val content = IO.read(script)
      assert(
        content.startsWith(header),
        "script doesn't start with #!/bin/sh header:\n" + script + "\n" + content)
      assert(
        header.r.findAllIn(content).length == 1,
        "script contains more than one header line:\n" + script + "\n" + content)
    }
    out.log.success("Successfully tested systemV control files")
    ()
}
