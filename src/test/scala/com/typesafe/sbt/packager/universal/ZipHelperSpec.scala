package com.typesafe.sbt.packager.universal

import com.typesafe.sbt.packager.DeleteDirectoryVisitor
import com.typesafe.sbt.packager.permissions
import org.scalatest._
import java.nio.file.{ Path, Paths, Files }
import java.nio.file.attribute.PosixFilePermission._

class ZipHelperSpec extends FlatSpec with Matchers with BeforeAndAfterAll {

  var tmp: Path = _

  override def beforeAll {
    tmp = Files createTempDirectory "_sbt-native-packager"
  }

  override def afterAll {
    Files.walkFileTree(tmp, new DeleteDirectoryVisitor)
  }

  "The zip helper" should "create a zip with a single file" in {
    // setup
    val out = tmp resolve "single.zip"
    val file = tmp resolve "single.txt"
    Files createFile file

    ZipHelper.zip(List(file.toFile -> "single.txt"), out.toFile)
    Files exists out should be(true)

    ZipHelper.withZipFilesystem(out.toFile) { system =>
      val zippedFile = system getPath "single.txt"
      Files exists zippedFile should be(true)
    }
  }

  it should "create a zip with nested directories" in {
    // setup
    val out = tmp resolve "single.zip"
    val dir = tmp resolve "dir"
    val nested = dir resolve "nested"
    Files createDirectories nested

    ZipHelper.zip(List(nested.toFile -> "dir/nested"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile) { system =>
      val zDir = system getPath "dir"
      Files exists zDir should be(true)
      Files isDirectory zDir should be(true)

      val zNested = zDir resolve "nested"
      Files exists zNested should be(true)
      Files isDirectory zNested should be(true)
    }
  }

  it should "create a zip with nested directories containing file" in {
    // setup
    val out = tmp resolve "single.zip"
    val dir = tmp resolve "dir"
    val file = dir resolve "file.txt"
    Files createDirectories dir
    Files createFile file

    ZipHelper.zip(List(file.toFile -> "dir/file.txt"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile) { system =>
      val zDir = system getPath "dir"
      Files exists zDir should be(true)
      Files isDirectory zDir should be(true)

      val zFile = zDir resolve "file.txt"
      Files exists zFile should be(true)
      Files isDirectory zFile should be(false)
    }
  }

  it should "create directories if necessary" in {
    // setup
    val out = tmp resolve "dir-creation.zip"
    val file = tmp resolve "dir-file.txt"
    Files createFile file

    ZipHelper.zip(List(file.toFile -> "dir/file.txt"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile) { system =>
      val zDir = system getPath "dir"
      Files exists zDir should be(true)
      Files isDirectory zDir should be(true)

      val zFile = zDir resolve "file.txt"
      Files exists zFile should be(true)
      Files isDirectory zFile should be(false)
    }

  }

  /*
   * This is currently not possible.
   */
  it should "preserve the executable bit" ignore {
    // setup
    val out = tmp resolve "exec.zip"
    val exec = tmp resolve "exec"
    Files createFile exec
    Files.setPosixFilePermissions(exec, permissions("0755"))

    val perms = Files getPosixFilePermissions exec
    perms should contain only (OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)

    ZipHelper.zip(List(exec.toFile -> "exec"), out.toFile)
    Files exists out should be(true)

    val unzipped = tmp resolve "unzipped-exec"
    ZipHelper.withZipFilesystem(out.toFile) { system =>
      val zippedFile = system getPath "exec"
      Files exists zippedFile should be(true)

      Files.copy(zippedFile, unzipped)
    }

    // checking permissions
    val unzippedPerms = Files getPosixFilePermissions unzipped
    unzippedPerms should contain only (OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)

  }

}
