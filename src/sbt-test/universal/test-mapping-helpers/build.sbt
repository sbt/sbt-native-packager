import com.typesafe.sbt.packager.MappingsHelper._

enablePlugins(JavaAppPackaging)

name := "simple-test"

version := "0.1.0"

// or just place your cache folder in /src/universal/
mappings in Universal ++= directory("src/main/resources/cache")

// or just place your cache folder in /src/universal/
mappings in Universal ++= contentOf("src/main/resources/docs")

mappings in Universal += {
  (packageBin in Compile).value
  // we are using the reference.conf as default application.conf
  // the user can override settings here
  val conf = sourceDirectory.value / "main" / "resources" / "reference.conf"
  conf -> "conf/application.conf"
}

TaskKey[Unit]("unzip") := {
  val args = Seq((packageBin in Universal).value.getAbsolutePath)
  Process("unzip", args) ! streams.value.log
}
