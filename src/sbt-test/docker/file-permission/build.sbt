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
      assertEquals(lines.take(2),
        """FROM fabric8/java-centos-openjdk8-jdk as stage0
          |LABEL snp-multi-stage="intermediate"""".stripMargin.linesIterator.toList)
      assert(lines(2).substring(0, 25) == "LABEL snp-multi-stage-id=") // random generated id is hard to test
      assertEquals(lines.drop(3),
        """WORKDIR /opt/docker
          |COPY 1 /1/
          |COPY 2 /2/
          |USER root
          |RUN ["chmod", "-R", "u=rX,g=rX", "/1/opt/docker"]
          |RUN ["chmod", "-R", "u=rX,g=rX", "/2/opt/docker"]
          |RUN ["chmod", "u+x,g+x", "/1/opt/docker/bin/file-permission-test"]
          |
          |FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 1>/dev/null 2>&1 || (( getent group 0 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 0 root || addgroup -g 0 -S root )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 || adduser -S -u 1001 -G root demiourgos728 ))
          |WORKDIR /opt/docker
          |COPY --from=stage0 --chown=demiourgos728:root /1/opt/docker /opt/docker
          |COPY --from=stage0 --chown=demiourgos728:root /2/opt/docker /opt/docker
          |USER 1001:0
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyNone := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 1>/dev/null 2>&1 || (( getent group 0 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 0 root || addgroup -g 0 -S root )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 || adduser -S -u 1001 -G root demiourgos728 ))
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER 1001:0
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyNoneGid := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 1>/dev/null 2>&1 || (( getent group 5000 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 5000 sbt || addgroup -g 5000 -S sbt )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 5000 demiourgos728 || adduser -S -u 1001 -G sbt demiourgos728 ))
          |WORKDIR /opt/docker
          |COPY opt /opt
          |USER 1001:5000
          |ENTRYPOINT ["/opt/docker/bin/file-permission-test"]
          |CMD []""".stripMargin.linesIterator.toList)
    },

    checkDockerfileWithStrategyRun := {
      val dockerfile = IO.read((stagingDirectory in Docker).value / "Dockerfile")
      val lines = dockerfile.linesIterator.toList
      assertEquals(lines,
        """FROM openjdk:8
          |USER root
          |RUN id -u demiourgos728 1>/dev/null 2>&1 || (( getent group 0 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 0 root || addgroup -g 0 -S root )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 || adduser -S -u 1001 -G root demiourgos728 ))
          |WORKDIR /opt/docker
          |COPY opt /opt
          |RUN ["chmod", "-R", "u=rX,g=rX", "/opt/docker"]
          |RUN ["chmod", "u+x,g+x", "/opt/docker/bin/file-permission-test"]
          |USER 1001:0
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
      assertEquals(lines.take(2),
        """FROM fabric8/java-centos-openjdk8-jdk as stage0
          |LABEL snp-multi-stage="intermediate"""".stripMargin.linesIterator.toList)
      assert(lines(2).substring(0, 25) == "LABEL snp-multi-stage-id=") // random generated id is hard to test
      assertEquals(lines.drop(3),
        """WORKDIR /opt/docker
          |COPY opt /opt
          |USER root
          |RUN ["chmod", "-R", "u=rwX,g=rwX", "/opt/docker"]
          |RUN ["chmod", "u+x,g+x", "/opt/docker/bin/file-permission-test"]
          |
          |FROM fabric8/java-centos-openjdk8-jdk
          |USER root
          |RUN id -u demiourgos728 1>/dev/null 2>&1 || (( getent group 0 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 0 root || addgroup -g 0 -S root )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 || adduser -S -u 1001 -G root demiourgos728 ))
          |WORKDIR /opt/docker
          |COPY --from=stage0 --chown=demiourgos728:root /opt/docker /opt/docker
          |USER 1001:0
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
