enablePlugins(ClasspathJarPlugin)

name := "classpath-jar-test"

version := "0.1.0"

// test dependencies sample
libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-kernel" % "2.3.4")

TaskKey[Unit]("check-classspath") := {
  val dir = (stagingDirectory in Universal).value
  val bat = IO.read(dir / "bin" / "classpath-jar-test.bat")
  assert(bat contains "set \"APP_CLASSPATH=%APP_LIB_DIR%\\classpath-jar-test.classpath-jar-test-0.1.0-classpath.jar\"")
  val jar = new java.util.jar.JarFile(dir / "lib" / "classpath-jar-test.classpath-jar-test-0.1.0-classpath.jar")
  assert(
    jar.getManifest().getMainAttributes().getValue("Class-Path").toString() contains "com.typesafe.akka.akka-actor"
  )
  jar close
}

TaskKey[Unit]("run-check") := {
  val dir = (stagingDirectory in Universal).value
  val cmd = if (System.getProperty("os.name").contains("Windows")) {
    Seq("cmd", "/c", (dir / "bin" / "classpath-jar-test.bat").getAbsolutePath)
  } else {
    Seq((dir / "bin" / "classpath-jar-test").getAbsolutePath)
  }
  assert(Process(cmd).!! contains "SUCCESS!")
}
