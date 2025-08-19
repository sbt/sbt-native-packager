import com.typesafe.sbt.packager.Keys.stagingDirectory
import com.typesafe.sbt.packager.PluginCompat
import xsbti.FileConverter

enablePlugins(JavaAppPackaging)

organization := "com.example"
name := "docker-groups"
version := "0.1.0"

Docker / dockerPackageMappings ++= {
  implicit val converter: FileConverter = fileConverter.value
  PluginCompat.toFileRefsMapping(
    Seq(
      (baseDirectory.value / "docker" / "spark-env.sh") -> "/opt/docker/spark/spark-env.sh",
      (baseDirectory.value / "docker" / "log4j.properties") -> "/opt/docker/other/log4j.properties"
    )
  )
}

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"

TaskKey[Unit]("checkDockerfile") := {
  val dockerfile = IO.read((Docker / stagingDirectory).value / "Dockerfile")
  val copyLines = dockerfile.linesIterator.toList.filter(_.startsWith("COPY --from=stage0"))
  assertEquals(
    copyLines,
    """COPY --from=stage0 --chown=demiourgos728:root /2/opt/docker /opt/docker
      |COPY --from=stage0 --chown=demiourgos728:root /4/opt/docker /opt/docker
      |COPY --from=stage0 --chown=demiourgos728:root /54/opt/docker /opt/docker
      |COPY --from=stage0 --chown=demiourgos728:root /opt/docker /opt/docker""".stripMargin.linesIterator.toList
  )
}

TaskKey[Unit]("checkDockerfileWithNoLayers") := {
  val dockerfile = IO.read((Docker / stagingDirectory).value / "Dockerfile")
  val copyLines = dockerfile.linesIterator.toList.filter(_.startsWith("COPY --from=stage0"))
  assertEquals(
    copyLines,
    """COPY --from=stage0 --chown=demiourgos728:root /opt/docker /opt/docker""".stripMargin.linesIterator.toList
  )
}

def assertEquals(left: List[String], right: List[String]) =
  assert(
    left == right,
    "\n" + ((left zip right) flatMap { case (a: String, b: String) =>
      if (a == b) Nil
      else List("- " + a, "+ " + b)
    }).mkString("\n")
  )
