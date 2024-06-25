package com.typesafe.sbt.packager

import org.scalatest._
import java.nio.file.attribute.PosixFilePermission._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FileUtilSpec extends AnyFlatSpec with Matchers {

  "permissions" should "convert octal to symbolic correctly" taggedAs (LinuxTag, WindowsTag) in {
    permissions convert "0000" should be("---------")
    permissions convert "0600" should be("rw-------")
    permissions convert "0755" should be("rwxr-xr-x")
    permissions convert "0777" should be("rwxrwxrwx")
  }

  it should "generate valid java PosixFilePermission" taggedAs (LinuxTag, WindowsTag) in {
    permissions("0000") should be(empty)

    val perm1 = permissions("0600")
    perm1 should not be (empty)
    perm1 should contain only (OWNER_READ, OWNER_WRITE)

    val perm2 = permissions("0755")
    perm2 should not be (empty)
    perm2 should contain only (OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)
  }

  "oct" should "parse octal string and convert to an integer" taggedAs (LinuxTag, WindowsTag) in {
    import permissions._
    oct"0000" should equal(0)
    oct"0" should equal(0)
    oct"777" should equal(511)
    oct"0777" should equal(511)
    oct"070" should equal(56)
    oct"123" should equal(83)

    a[NumberFormatException] should be thrownBy oct"foobar"
  }
}
