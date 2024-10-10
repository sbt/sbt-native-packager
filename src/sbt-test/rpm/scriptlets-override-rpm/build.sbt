import com.typesafe.sbt.packager.Compat._

enablePlugins(JavaServerAppPackaging, SystemVPlugin)

name := "rpm-test"
version := "0.1.0"
maintainer := "Josh Suereth <joshua.suereth@typesafe.com>"

packageSummary := "Test rpm package"
packageDescription := "Description"

rpmRelease := "1"
rpmVendor := "typesafe"
rpmUrl := Some("http://github.com/sbt/sbt-native-packager")
rpmLicense := Some("BSD")

(Compile / run / mainClass) := Some("com.example.MainApp")

TaskKey[Unit]("unzipAndCheck") := {
  val rpmFile = ((Rpm / packageBin)).value
  val rpmPath = Seq(rpmFile.getAbsolutePath)
  sys.process.Process("rpm2cpio", rpmPath) #| sys.process.Process("cpio -i --make-directories") ! streams.value.log
  val scriptlets = sys.process.Process("rpm -qp --scripts " + rpmFile.getAbsolutePath) !! streams.value.log
  assert(scriptlets contains "echo postinst", "'echo 'postinst' not present in \n" + scriptlets)
  assert(scriptlets contains "echo preinst", "'echo 'preinst' not present in \n" + scriptlets)
  assert(scriptlets contains "echo postun", "'echo 'postun' not present in \n" + scriptlets)
  assert(scriptlets contains "echo preun", "'echo 'preun' not present in \n" + scriptlets)
  ()
}

TaskKey[Unit]("checkSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(spec contains "echo postinst", "'echo 'postinst' not present in \n" + spec)
  assert(spec contains "echo preinst", "'echo 'preinst' not present in \n" + spec)
  assert(spec contains "echo postun", "'echo 'postun' not present in \n" + spec)
  assert(spec contains "echo preun", "'echo 'preun' not present in \n" + spec)
  ()
}

def countSubstring(str: String, substr: String): Int =
  substr.r.findAllMatchIn(str).length

def isUnique(str: String, searchstr: String): Boolean =
  countSubstring(str, searchstr) == 1

TaskKey[Unit]("uniqueSccriptsInSpecFile") := {
  val spec = IO.read(target.value / "rpm" / "SPECS" / "rpm-test.spec")
  assert(isUnique(spec, "echo postinst"), "'echo 'postinst' not unique in \n" + spec)
  assert(isUnique(spec, "echo preinst"), "'echo 'preinst' not unique in \n" + spec)
  assert(isUnique(spec, "echo postun"), "'echo 'postun' not unique in \n" + spec)
  assert(isUnique(spec, "echo preun"), "'echo 'preun' not unique in \n" + spec)
  ()
}
