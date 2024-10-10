import com.typesafe.sbt.packager.Compat._

import com.typesafe.sbt.packager.MappingsHelper._

enablePlugins(JavaAppPackaging)

name := "simple-test"

version := "0.1.0"

// or just place your cache folder in /src/universal/
(Universal / mappings) ++= directory("src/main/resources/cache")

// or just place your cache folder in /src/universal/
(Universal / mappings) ++= contentOf("src/main/resources/docs")

(Universal / mappings) += {
  ((Compile / packageBin)).value
  // we are using the reference.conf as default application.conf
  // the user can override settings here
  val conf = sourceDirectory.value / "main" / "resources" / "reference.conf"
  conf -> "conf/application.conf"
}

TaskKey[Unit]("unzip") := {
  val args = Seq(((Universal / packageBin)).value.getAbsolutePath)
  sys.process.Process("unzip", args) ! streams.value.log
}
