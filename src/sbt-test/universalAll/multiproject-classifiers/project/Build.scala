import sbt._
import Keys._
import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._

object MutliBuild extends Build {
  
  val appVersion = "1.0"
    
  val mySettings: Seq[Setting[_]] =
    packageArchetype.java_application ++
    Seq(
      organization := "org.test",
      version := appVersion,
      TaskKey[Unit]("show-files") <<= (name, target, streams) map { (n, t, s) =>
        System.out.synchronized {
          println("Files in ["+n+"]")
          val files = (t / "universal/stage").***.get
          files foreach println
        }
      }
    )

  val assets = config("assets")

  lazy val sub = (
    Project("sub", file("sub"))
    settings(mySettings:_*)
    settings(
      ivyConfigurations += assets,
      artifact in assets := artifact.value.copy(classifier = Some("assets")),
      packagedArtifacts += {
        val file = target.value / "assets.jar"
        val assetsDir = baseDirectory.value / "src" / "main" / "assets"
        val sources = assetsDir.***.filter(_.isFile) pair relativeTo(assetsDir)
        IO.zip(sources, file)
        (artifact in assets).value -> file
      },
      exportedProducts in assets := {
        Seq(
          Attributed.blank(baseDirectory.value / "src" / "main" / "assets")
            .put(artifact.key, (artifact in assets).value)
            .put(AttributeKey[ModuleID]("module-id"), projectID.value)
        )
      }
    )
  )
 
  lazy val root = (
    Project("root", file("."))
    settings(mySettings:_*)
  	dependsOn(sub % "compile->compile;compile->assets")
  )

}
