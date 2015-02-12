package com.typesafe.sbt.packager

import org.scalatest._
import java.nio.file.attribute.PosixFilePermission._

class FileUtilSpec extends FlatSpec with Matchers {

  "permissions" should "convert octal to symbolic correctly" in {
    permissions convert "0000" should be("---------")
    permissions convert "0600" should be("rw-------")
    permissions convert "0755" should be("rwxr-xr-x")
    permissions convert "0777" should be("rwxrwxrwx")
  }

  it should "generate valid java PosixFilePermission" in {
    permissions("0000") should be(empty)

    val perm1 = permissions("0600")
    perm1 should not be (empty)
    perm1 should contain only (OWNER_READ, OWNER_WRITE)

    val perm2 = permissions("0755")
    perm2 should not be (empty)
    perm2 should contain only (OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)

  }

}