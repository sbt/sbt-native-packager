package com.typesafe.sbt.packager.docker

/**
  * This class represents a Docker alias.
  * It generates a string in the form of {{{[REGISTRY_HOST/][USERNAME/]NAME[:TAG]}}},
  * e.g. ''my-registry.com:1234/my-user/my-service:1.0.0'' or just ''my-service:1.0.0''.
  * @param registryHost Optional hostname of the registry (including port if applicable)
  * @param username Optional username or other qualifier
  * @param name Name of the image, e.g. the artifact name
  * @param tag Optional tag for the image, e.g. the version
  */
case class DockerAlias(registryHost: Option[String], username: Option[String], name: String, tag: Option[String]) {

  def withRegistryHost(registryHost: Option[String]): DockerAlias = copy(registryHost = registryHost)

  def withUsername(username: Option[String]): DockerAlias = copy(username = username)

  def withName(name: String): DockerAlias = copy(name = name)

  def withTag(tag: Option[String]): DockerAlias = copy(tag = tag)

  override def toString: String =
    registryHost.map(_ + "/").getOrElse("") +
      username.map(_ + "/").getOrElse("") +
      name +
      tag.map(":" + _).getOrElse("")
}
