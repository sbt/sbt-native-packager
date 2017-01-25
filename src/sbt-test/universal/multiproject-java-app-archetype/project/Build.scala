import sbt._
import Keys._
import com.typesafe.sbt.SbtNativePackager._

// FIXME replace Build with AutoPlugin
object MutliBuild extends Build {

  val appName = "play-bug-1499"
  val appVersion = "1.0"

  val mySettings: Seq[Setting[_]] =
    packageArchetype.java_application ++
      Seq(organization := "org.test", version := appVersion, TaskKey[Unit]("show-files") := {
        System.out.synchronized {
          println("Files in [" + name.value + "]")
          val files = (target.value / "universal/stage").***.get
          files foreach println
        }
      })

  lazy val common = (
      Project(appName + "-common", file("module/common"))
        settings (mySettings: _*)
    )

  lazy val foo = (
      Project(appName + "-foo", file("module/foo"))
        settings (mySettings: _*)
        dependsOn (common)
    )

  lazy val bar = (
      Project(appName + "-bar", file("module/bar"))
        settings (mySettings: _*)
        dependsOn (common)
    )

  lazy val aaMain = (
      Project(appName + "-main", file("."))
        settings (mySettings: _*)
        dependsOn (common, foo, bar)
        aggregate (foo, bar)
    )

}
