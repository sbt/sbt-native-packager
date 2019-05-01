lazy val checkDockerfileDefaults = taskKey[Unit]("")
lazy val checkDockerfileWithStrategyNone = taskKey[Unit]("")
lazy val checkDockerfileWithStrategyNoneGid = taskKey[Unit]("")
lazy val checkDockerfileWithStrategyRun = taskKey[Unit]("")
lazy val checkDockerfileWithStrategyCopyChown = taskKey[Unit]("")
lazy val checkDockerfileWithWriteExecute = taskKey[Unit]("")

lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(
    name := "file-permission-test",
    version := "0.1.0",
    checkDockerfileDefaults := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk as stage0
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER root
          |RUN ["chmod", "-R", "u=rX,g=rX", "/opt/docker"]
          |RUN ["chmod", "u+x,g+x", "/opt/docker/bin/file-permission-test"]
          |
          |FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 2> /dev/null || (( getent group 0 || groupadd -g 0 root ) && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 )
          |WORKDIR /opt/docker
          |COPY --from=stage0 --chown=demiourgos728:root /opt/docker /opt/docker
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyNone := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 2> /dev/null || (( getent group 0 || groupadd -g 0 root ) && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 )
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyNoneGid := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 2> /dev/null || (( getent group 5000 || groupadd -g 5000 sbt ) && useradd --system --create-home --uid 1001 --gid 5000 demiourgos728 )
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyRun := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8
          |USER root
          |RUN id -u demiourgos728 2> /dev/null || (( getent group 0 || groupadd -g 0 root ) && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 )
          |WORKDIR /opt/docker
          |COPY opt /opt
          |RUN ["chmod", "-R", "u=rX,g=rX", "/opt/docker"]
          |RUN ["chmod", "u+x,g+x", "/opt/docker/bin/file-permission-test"]
          |USER 1001
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyCopyChown := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk
          |WORKDIR /opt/docker
          |COPY --chown=daemon:root opt /opt
          |USER daemon
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithWriteExecute := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk as stage0
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER root
          |RUN ["chmod", "-R", "u=rwX,g=rwX", "/opt/docker"]
          |RUN ["chmod", "u+x,g+x", "/opt/docker/bin/file-permission-test"]
          |
          |FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 2> /dev/null || (( getent group 0 || groupadd -g 0 root ) && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 )
          |WORKDIR /opt/docker
          |COPY --from=stage0 --chown=demiourgos728:root /opt/docker /opt/docker
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
