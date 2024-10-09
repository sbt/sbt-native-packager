enablePlugins(LauncherJarPlugin)

name := "launcher-jar-test"

version := "0.1.0"

// test dependencies sample
libraryDependencies += "com.typesafe" % "config" % "1.3.1"

TaskKey[Unit]("checkClasspath") := {
  val dir = (Universal / stagingDirectory).value
  val bat = IO.read(dir / "bin" / "launcher-jar-test.bat")
  assert(bat contains "set \"APP_CLASSPATH=\"", "bat should set APP_CLASSPATH:\n" + bat)
  assert(
    bat contains "set \"APP_MAIN_CLASS=-jar \"%APP_LIB_DIR%\\launcher-jar-test.launcher-jar-test-0.1.0-launcher.jar\"\"",
    "bat should set APP_MAIN_CLASS:\n" + bat
  )
  val bash = IO.read(dir / "bin" / "launcher-jar-test")
  assert(bash contains "declare -r app_classpath=\"\"", "bash should declare app_classpath:\n" + bash)
  assert(
    bash contains "declare -a app_mainclass=(-jar \"$lib_dir/launcher-jar-test.launcher-jar-test-0.1.0-launcher.jar\")",
    "bash should declare app_mainclass:\n" + bash
  )
  val jar = new java.util.jar.JarFile(dir / "lib" / "launcher-jar-test.launcher-jar-test-0.1.0-launcher.jar")
  val attributes = jar.getManifest().getMainAttributes()
  assert(
    attributes.getValue("Class-Path").toString() contains "com.typesafe.config",
    "MANIFEST Class-Path should contain com.typesafe.config:\n" + attributes.getValue("Class-Path").toString()
  )
  assert(
    attributes.getValue("Main-Class").toString() contains "test.Test",
    "MANIFEST Main-Class should contain test.Test:\n" + attributes.getValue("Main-Class").toString()
  )
  jar close
}

TaskKey[Unit]("runCheck") := {
  val dir = (Universal / stagingDirectory).value
  val cmd = if (System.getProperty("os.name").contains("Windows")) {
    Seq("cmd", "/c", (dir / "bin" / "launcher-jar-test.bat").getAbsolutePath)
  } else {
    Seq((dir / "bin" / "launcher-jar-test").getAbsolutePath)
  }
  assert(sys.process.Process(cmd).!! contains "SUCCESS!")
}
