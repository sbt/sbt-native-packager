name := "test-layer-groups-playframework"
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"

enablePlugins(DockerPlugin, LauncherJarPlugin)

// generate a conf/application.ini
Universal / javaOptions ++= Seq("-J-Xms1024m", "-J-Xmx1024m")

TaskKey[Unit]("checkDockerLayers") := {
  val layers = (Docker / dockerLayerMappings).value.groupBy(_.layerId).withDefaultValue(Nil)

  val layer10 = layers(Some(10))
  assert(layer10.isEmpty, "layer 10 should be empty because jlink is not used")

  val layer20 = layers(Some(20))
  assert(
    layer20.forall(_.file.getPath.startsWith(csrCacheDirectory.value.getPath)),
    "layer 20 should only contain external libraries"
  )

  val layer30 = layers(Some(30))
  assert(layer30.exists(_.path == "/opt/docker/conf/application.ini"), "layer 30 should contain application.ini")
  assert(layer30.exists(_.path == "/opt/docker/conf/logback.xml"), "layer 30 should contain logback.xml")

  val layer40 = layers(Some(40))
  assert(
    layer40.exists(_.file == (Runtime / PlayKeys.playJarSansExternalized).value),
    "layer 40 should contain Play's -sans-externalized.jar"
  )
  assert(layer40.exists(_.file == PlayKeys.playPackageAssets.value), "layer 40 should contain Play's -assets.jar")
  assert(layer40.exists(_.file == packageJavaLauncherJar.value), "layer 40 should contain launcher jar")
  assert(
    layer40.exists(layer => makeBashScripts.value.map(_._1).contains(layer.file)),
    "layer 40 should contain start scripts"
  )

  val layerFinal = layers(None)
  assert(layerFinal.exists(_.path.startsWith("/opt/docker/share/doc/api/")), "final layer should contain API docs")
}
