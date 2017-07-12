import com.typesafe.sbt.packager.Compat._

enablePlugins(ClasspathJarPlugin)

name := "classpath-jar-test"

version := "0.1.0"

// test dependencies sample
libraryDependencies += "com.typesafe" % "config" % "1.3.1"

TaskKey[Unit]("checkClasspath") := {
  val dir = (stagingDirectory in Universal).value
  val bat = IO.read(dir / "bin" / "classpath-jar-test.bat")
  assert(bat contains "set \"APP_CLASSPATH=%APP_LIB_DIR%\\classpath-jar-test.classpath-jar-test-0.1.0-classpath.jar\"")
  val jar = new java.util.jar.JarFile(dir / "lib" / "classpath-jar-test.classpath-jar-test-0.1.0-classpath.jar")
  assert(jar.getManifest().getMainAttributes().getValue("Class-Path").toString() contains "com.typesafe.config")
  jar close
}

TaskKey[Unit]("runCheck") := {
  val dir = (stagingDirectory in Universal).value
  val cmd = if (System.getProperty("os.name").contains("Windows")) {
    Seq("cmd", "/c", (dir / "bin" / "classpath-jar-test.bat").getAbsolutePath)
  } else {
    Seq((dir / "bin" / "classpath-jar-test").getAbsolutePath)
  }
  assert(sys.process.Process(cmd).!! contains "SUCCESS!")
}
