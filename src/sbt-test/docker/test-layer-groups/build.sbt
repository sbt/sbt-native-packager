enablePlugins(JavaAppPackaging)

organization := "com.example"
name := "docker-groups"
version := "0.1.0"

dockerPackageMappings in Docker ++= Seq(
  (baseDirectory.value / "docker" / "spark-env.sh") -> "/opt/docker/spark/spark-env.sh",
  (baseDirectory.value / "docker" / "log4j.properties") -> "/opt/docker/other/log4j.properties"
)

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"