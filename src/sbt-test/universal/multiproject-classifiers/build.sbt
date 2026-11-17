import sbtcompat.PluginCompat
import xsbti.FileConverter

lazy val appVersion = "1.0"

lazy val mySettings: Seq[Setting[?]] =
  Seq(
    organization := "org.test",
    version := appVersion,
    TaskKey[Unit]("showFiles") :=
      System.out.synchronized {
        println("Files in [" + name.value + "]")
        val files = (target.value / "universal/stage").**(AllPassFilter).get()
        files.foreach(println)
      }
  )

lazy val Assets = config("assets")

// Declare these with the macro so sbt 2 uses the same sequence-key tags as its built-in keys.
lazy val exportedProductsVersioned = taskKey[Classpath]("Versioned asset exports")
lazy val exportedProductsVersionedIfMissing = taskKey[Classpath]("Versioned asset exports if missing")
lazy val exportedProductsVersionedNoTracking = taskKey[Classpath]("Versioned asset exports without tracking")

lazy val sub = project
  .in(file("sub"))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .settings(
    ivyConfigurations += Assets,
    Assets / artifact := artifact.value.withClassifier(classifier = Some("assets")),
    packagedArtifacts += {
      implicit val converter: FileConverter = fileConverter.value
      val file = target.value / "assets.jar"
      val assetsDir = baseDirectory.value / "src" / "main" / "assets"
      val sources = assetsDir.**(AllPassFilter).filter(_.isFile).pair(file => IO.relativize(assetsDir, file))
      IO.zip(sources, file, None)
      (Assets / artifact).value -> PluginCompat.toFileRef(file)
    },
    // sbt 2 uses versioned exports on runtime inter-project classpaths.
    Assets / exportedProductsVersioned := (Assets / exportedProducts).value,
    Assets / exportedProductsVersionedIfMissing := (Assets / exportedProducts).value,
    Assets / exportedProductsVersionedNoTracking := (Assets / exportedProducts).value,
    Assets / exportedProducts := {
      implicit val converter: FileConverter = fileConverter.value
      val assetsDir = baseDirectory.value / "src" / "main" / "assets"
      assetsDir
        .**(AllPassFilter)
        .filter(_.isFile)
        .classpath
        .map(
          _.put(PluginCompat.artifactStr, PluginCompat.artifactToStr((Assets / artifact).value))
            .put(PluginCompat.moduleIDStr, PluginCompat.moduleIDToStr(projectID.value))
        )
    }
  )

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .dependsOn(sub % "compile->compile;compile->assets")
