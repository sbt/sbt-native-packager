package com.typesafe.sbt.packager.linux

import java.io.File

import org.scalatest.{Matchers, WordSpec}

/**
  * Created by carsten on 13.10.16.
  */
class LinuxMappingDSLSpec
  extends WordSpec
    with Matchers with LinuxMappingDSL {

  "The LinuxMappingDSL" should {

    "map config files to noreplace" in {
      val f1 = LinuxPackageMapping(Map(new File("/tmp/1") -> "/tmp/1"))
      val f2 = LinuxPackageMapping(Map(new File("/tmp/1") -> "/tmp/1")).withConfig()

      val f1Mapped :: f2Mapped :: Nil = configWithNoReplace(Seq(f1, f2))

      f1Mapped.fileData.config should be ("false")
      f2Mapped.fileData.config should be ("noreplace")
    }
  }
}
