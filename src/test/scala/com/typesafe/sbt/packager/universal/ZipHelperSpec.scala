package com.typesafe.sbt.packager.universal

import com.typesafe.sbt.packager.DeleteDirectoryVisitor
import com.typesafe.sbt.packager.permissions
import org.scalatest._
import java.io.File
import java.nio.file.{ Path, Paths, Files }
import java.nio.file.attribute.PosixFilePermission._
import scala.collection.JavaConversions._

class ZipHelperSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

  var tmp: Path = _

  override def beforeEach {
    tmp = Files createTempDirectory "_sbt-native-packager"
  }

  override def afterEach {
    Files.walkFileTree(tmp, new DeleteDirectoryVisitor)
  }

  "The ZipHelper.zip" should "create a zip with a single file" in {
    zipSingleFile(ZipHelper.zip)
  }

  // ignores empty directories
  it should "create a zip with nested directories" ignore {
    zipNestedFile(ZipHelper.zip)
  }

  it should "create a zip with nested directories containing file" in {
    zipNestedDirsWithFiles(ZipHelper.zip)
  }

  it should "create directories if necessary" in {
    createNecessaryDirectories(ZipHelper.zip)
  }

  // works only on some systems
  it should "preserve the executable bit" ignore {
    preserveExecutableBit(ZipHelper.zip)
  }

  "The ZipHelper.zipNIO" should "create a zip with a single file" in {
    zipSingleFile(ZipHelper.zipNIO)
  }

  it should "create a zip with nested directories" in {
    zipNestedFile(ZipHelper.zipNIO)
  }

  it should "create a zip with nested directories containing file" in {
    zipNestedDirsWithFiles(ZipHelper.zipNIO)
  }

  it should "create directories if necessary" in {
    createNecessaryDirectories(ZipHelper.zipNIO)
  }

  // never works
  it should "preserve the executable bit" ignore {
    preserveExecutableBit(ZipHelper.zipNIO)
  }

  "The ZipHelper.zipNative" should "create a zip with a single file" in {
    zipSingleFile(ZipHelper.zipNative)
  }

  it should "create a zip with nested directories" in {
    zipNestedFile(ZipHelper.zipNative)
  }

  it should "create a zip with nested directories containing file" in {
    zipNestedDirsWithFiles(ZipHelper.zipNative)
  }

  it should "create directories if necessary" in {
    createNecessaryDirectories(ZipHelper.zipNative)
  }

  // never works
  it should "preserve the executable bit" ignore {
    preserveExecutableBit(ZipHelper.zipNative)
  }

  /* ========================================================== */
  /* ========================================================== */
  /* ========================================================== */

  private type Zipper = (Traversable[(File, String)], File) => Unit

  private def zipSingleFile(zipper: Zipper) {
    val out = tmp resolve "single.zip"
    val file = tmp resolve "single.txt"
    Files createFile file

    zipper(List(file.toFile -> "single.txt"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile, false) { system =>
      val zippedFile = system getPath "single.txt"
      Files exists zippedFile should be(true)
    }
  }

  private def zipNestedFile(zipper: Zipper) {
    // setup
    val out = tmp resolve "nested.zip"
    val dir = tmp resolve "dir"
    val nested = dir resolve "nested"
    Files createDirectories nested

    zipper(List(nested.toFile -> "dir/nested"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile, false) { system =>

      val zDir = system getPath "dir"
      Files exists zDir should be(true)
      Files isDirectory zDir should be(true)

      val zNested = zDir resolve "nested"
      Files exists zNested should be(true)
      Files isDirectory zNested should be(true)
    }
  }

  private def zipNestedDirsWithFiles(zipper: Zipper) {
    // setup
    val out = tmp resolve "nested-containing.zip"
    val dir = tmp resolve "dir"
    val file = dir resolve "file.txt"
    Files createDirectories dir
    Files createFile file

    zipper(List(file.toFile -> "dir/file.txt"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile, false) { system =>
      val zDir = system getPath "dir"
      Files exists zDir should be(true)
      Files isDirectory zDir should be(true)

      val zFile = zDir resolve "file.txt"
      Files exists zFile should be(true)
      Files isDirectory zFile should be(false)
    }
  }

  private def createNecessaryDirectories(zipper: Zipper) {
    val out = tmp resolve "dir-creation.zip"
    val file = tmp resolve "dir-file.txt"
    Files createFile file

    zipper(List(file.toFile -> "dir/file.txt"), out.toFile)

    ZipHelper.withZipFilesystem(out.toFile, false) { system =>
      val zDir = system getPath "dir"
      Files exists zDir should be(true)
      Files isDirectory zDir should be(true)

      val zFile = zDir resolve "file.txt"
      Files exists zFile should be(true)
      Files isDirectory zFile should be(false)
    }
  }

  private def preserveExecutableBit(zipper: Zipper) {
    val out = tmp resolve "exec.zip"
    val exec = tmp resolve "exec"
    Files createFile exec
    Files.setPosixFilePermissions(exec, permissions("0755"))

    val perms = Files getPosixFilePermissions exec
    perms should contain only (OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)

    zipper(List(exec.toFile -> "exec"), out.toFile)
    Files exists out should be(true)

    val unzipped = tmp resolve "unzipped-exec"
    ZipHelper.withZipFilesystem(out.toFile, false) { system =>
      val zippedFile = system getPath "exec"
      Files exists zippedFile should be(true)

      Files.copy(zippedFile, unzipped)
    }

    // checking permissions
    val unzippedPerms = Files getPosixFilePermissions unzipped
    unzippedPerms should contain only (OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ, GROUP_EXECUTE, OTHERS_READ, OTHERS_EXECUTE)
  }

}
