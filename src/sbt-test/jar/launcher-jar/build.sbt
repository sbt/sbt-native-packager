enablePlugins(LauncherJarPlugin)

name := "launcher-jar-test"

version := "0.1.0"

// test dependencies sample
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-kernel" % "2.3.4"
)

TaskKey[Unit]("check-classpath") := {
  val dir = (stagingDirectory in Universal).value
  val bat = IO.read(dir / "bin" / "launcher-jar-test.bat")
  assert(bat contains "set \"APP_CLASSPATH=\"")
  assert(bat contains "set \"APP_MAIN_CLASS=-jar %APP_LIB_DIR%\\launcher-jar-test.launcher-jar-test-0.1.0-launcher.jar\"")
  val bash = IO.read(dir / "bin" / "launcher-jar-test")
  assert(bash contains "declare -r app_classpath=\"\"")
  assert(bash contains "declare -r app_mainclass=\"-jar $lib_dir/launcher-jar-test.launcher-jar-test-0.1.0-launcher.jar\"")
  val jar = new java.util.jar.JarFile(dir / "lib" / "launcher-jar-test.launcher-jar-test-0.1.0-launcher.jar")
  val attributes = jar.getManifest().getMainAttributes()
  assert(attributes.getValue("Class-Path").toString() contains "com.typesafe.akka.akka-actor")
  assert(attributes.getValue("Main-Class").toString() contains "test.Test")
  jar close
}

TaskKey[Unit]("run-check") := { 
  val dir = (stagingDirectory in Universal).value
  val cmd = if(System.getProperty("os.name").contains("Windows")){
    Seq("cmd", "/c", (dir / "bin" / "launcher-jar-test.bat").getAbsolutePath)
  }else{
    Seq((dir / "bin" / "launcher-jar-test").getAbsolutePath)
  }
  assert(Process(cmd).!! contains "SUCCESS!")
}
