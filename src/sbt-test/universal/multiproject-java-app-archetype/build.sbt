lazy val appName = "play-bug-1499"
lazy val appVersion = "1.0"

lazy val mySettings: Seq[Setting[_]] =
  Seq(organization := "org.test", version := appVersion, TaskKey[Unit]("showFiles") := {
    System.out.synchronized {
      println("Files in [" + name.value + "]")
      val files = (target.value / "universal/stage").**(AllPassFilter).get
      files foreach println
    }
  })

lazy val common = project
  .in(file("module/common"))
  .settings(mySettings)

lazy val foo = project
  .in(file("module/foo"))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .dependsOn(common)

lazy val bar = project
  .in(file("module/bar"))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .dependsOn(common)

lazy val aaMain = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .dependsOn(common, foo, bar)
  .aggregate(foo, bar)
