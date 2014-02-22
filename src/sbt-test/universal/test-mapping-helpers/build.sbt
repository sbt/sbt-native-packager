import NativePackagerKeys._
import com.typesafe.sbt.packager.MappingsHelper._

packagerSettings

packagerSettings

name := "simple-test"

version := "0.1.0"

// or just place your cache folder in /src/universal/
mappings in Universal ++= directory("src/main/resources/cache")

// or just place your cache folder in /src/universal/
mappings in Universal ++= contentOf("src/main/resources/docs")

mappings in Universal <+= (packageBin in Compile, sourceDirectory ) map { (_, src) =>
    // we are using the reference.conf as default application.conf
    // the user can override settings here
    val conf = src / "main" / "resources" / "reference.conf"
    conf -> "conf/application.conf"
}

TaskKey[Unit]("unzip") <<= (packageBin in Universal, streams) map { (zipFile, streams) =>
    val args = Seq(zipFile.getAbsolutePath)
    Process("unzip", args) !  streams.log
}
