lazy val checkDockerfile = taskKey[Unit]("")
lazy val checkDockerfile2 = taskKey[Unit]("")
lazy val checkDockerfile3 = taskKey[Unit]("")
lazy val checkDockerfile4 = taskKey[Unit]("")
lazy val checkDockerfile5 = taskKey[Unit]("")

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name := "file-permission-test",
    version := "0.1.0",
    checkDockerfile := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8 as stage0
          |WORKDIR /opt/docker
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |COPY opt /opt
          |RUN ["chmod", "-R", "u=rX,g=rX", "/opt/docker"]
          |
          |FROM openjdk:8
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |WORKDIR /opt/docker
          |COPY --from=stage0 --chown=daemon:root /opt/docker /opt/docker
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfile2 := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfile3 := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |WORKDIR /opt/docker
          |COPY opt /opt
          |RUN ["chmod", "-R", "u=rX,g=rX", "/opt/docker"]
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfile4 := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |WORKDIR /opt/docker
          |COPY --chown=daemon:root opt /opt
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfile5 := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8 as stage0
          |WORKDIR /opt/docker
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |COPY opt /opt
          |RUN ["chmod", "-R", "u=rwX,g=rwX", "/opt/docker"]
          |
          |FROM openjdk:8
          |RUN id -u daemon || useradd --system --create-home --uid 1001 --gid 0 daemon
          |WORKDIR /opt/docker
          |COPY --from=stage0 --chown=daemon:root /opt/docker /opt/docker
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    }
  )

def assertEquals(left: List[String], right: List[String]) =
  assert(left == right,
    "\n" + ((left zip right) flatMap { case (a: String, b: String) =>
      if (a == b) Nil
      else List("- " + a, "+ " + b)
    }).mkString("\n"))
