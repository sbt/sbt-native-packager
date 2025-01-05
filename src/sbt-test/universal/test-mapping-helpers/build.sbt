import com.typesafe.sbt.packager.Compat._
import com.typesafe.sbt.packager.PluginCompat

import com.typesafe.sbt.packager.MappingsHelper._
import xsbti.FileConverter

enablePlugins(JavaAppPackaging)

name := "simple-test"

version := "0.1.0"

// or just place your cache folder in /src/universal/
Universal / mappings ++= {
  implicit val converter: FileConverter = fileConverter.value
  PluginCompat.toFileRefsMapping(directory("src/main/resources/cache"))
}

// or just place your cache folder in /src/universal/
Universal / mappings ++= {
  implicit val converter: FileConverter = fileConverter.value
  PluginCompat.toFileRefsMapping(contentOf("src/main/resources/docs"))
}

Universal / mappings += {
  (Compile / packageBin).value
  implicit val converter: FileConverter = fileConverter.value
  // we are using the reference.conf as default application.conf
  // the user can override settings here
  val conf = sourceDirectory.value / "main" / "resources" / "reference.conf"
  PluginCompat.toFileRef(conf) -> "conf/application.conf"
}

TaskKey[Unit]("unzip") := {
  implicit val converter: FileConverter = fileConverter.value
  val args = Seq(PluginCompat.toFile((Universal / packageBin).value).getAbsolutePath)
  sys.process.Process("unzip", args) ! streams.value.log
}
