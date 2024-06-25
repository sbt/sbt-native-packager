import com.typesafe.sbt.packager.docker._

dockerVersion := Some(DockerVersion(1, 13, 0, None))
dockerApiVersion := Some(DockerApiVersion(1, 28))
