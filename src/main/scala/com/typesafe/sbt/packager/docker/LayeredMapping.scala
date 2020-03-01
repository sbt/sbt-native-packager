package com.typesafe.sbt.packager.docker

import java.io.File

/**
  * Mapping of file to layers
  *
  * @param layerId The identifier in the layer used to increase cache hits in
  *                docker caching. LayerId is present in docker:stage directory structure
  *                and in intermediate image produced in the multi-stage docker build.
  * @param file The file produced by universal/stage to be moved into docker/stage directory.
  * @param path The path in the final image
  */
case class LayeredMapping(layerId: Option[Int], file: File, path: String)
