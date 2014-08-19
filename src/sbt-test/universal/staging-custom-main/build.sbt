import NativePackagerKeys._

packageArchetype.java_application

name := "stage-custom-main"

executableScriptName := "stage-custom-main"

version := "0.1.0"

TaskKey[Unit]("unzip") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
    val args = Seq(zipFile.getAbsolutePath)
    Process("unzip", args) !  streams.log
}
